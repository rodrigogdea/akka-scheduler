package org.scheduler

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.event.Logging
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import org.joda.time.DateTime
import org.scheduler.SimpleScheduler.{ScheduleJob, Start, Stop, Tick, UnscheduleJob}

import scala.concurrent.Future
import scala.concurrent.duration._

object SimpleScheduler {

  private implicit lazy val system: ActorSystem = ActorSystem("SimpleSchedulerSystem")
  private implicit lazy val materializer: Materializer = ActorMaterializer()

  private lazy val schedulerActor: ActorRef = system.actorOf(Props(new SimpleScheduler()), "SimpleScheduler")

  def scheduleJob(cronExpression: CronExpression, job: Job): Unit = schedulerActor ! ScheduleJob(job, cronExpression)

  def unscheduleJob(jobName: String): Unit = schedulerActor ! UnscheduleJob(jobName)

  def start(): Unit = schedulerActor ! Start

  def stop(): Unit = schedulerActor ! Stop

  case class Tick(tick: DateTime)

  class Job(val name: String, task: => Unit) {
    def execute(): Unit = task
  }

  case class ScheduleJob(job: Job, cronExpression: CronExpression)

  case class UnscheduleJob(jobName: String)

  case object Start

  case object Stop

}

class SimpleScheduler(implicit materializer: Materializer) extends Actor {

  import context.dispatcher

  val logger = Logging(context.system, this)

  private val jobs = collection.mutable.Map[String, ScheduleJob]()

  private var tickCancellable: Cancellable = new Cancellable {
    def cancel(): Boolean = true

    def isCancelled: Boolean = true
  }

  override def receive: Receive = {

    case Start =>

      if (tickCancellable.isCancelled) {
        tickCancellable = context.system.scheduler.schedule(1 second, 1 second)(
          self ! Tick(DateTime.now())
        )
      }

    case Stop => tickCancellable.cancel()

    case Tick(tick) =>
      if (jobs.nonEmpty) {
        Source
          .fromIterator(() => jobs.valuesIterator)
          .filter(_.cronExpression.at(tick))
          .mapAsync(jobs.size)(job =>
            Future(job.job.execute())
              .recover {
                case e: Exception => logger.error(e, s"In Job: ${job.job.name}")
              }
          )
          .runWith(Sink.ignore)
      }

    case jobToSchedule: ScheduleJob =>
      jobs += (jobToSchedule.job.name -> jobToSchedule)

    case UnscheduleJob(jobName) =>
      jobs -= jobName
  }
}
