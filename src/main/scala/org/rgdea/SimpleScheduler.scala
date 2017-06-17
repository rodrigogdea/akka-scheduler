package org.rgdea

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props, Scheduler}
import akka.event.{DiagnosticLoggingAdapter, Logging}
import akka.routing.{BroadcastRoutingLogic, Router}
import org.joda.time.DateTime
import org.rgdea.SimpleScheduler.tickInterval

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait SimpleSchedulerExecutionContext {

  object ThreadPoolContext {

    import scala.concurrent.ExecutionContext

    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(null)
  }

}

object SimpleScheduler  {

  val tickInterval: Int = 500

  private lazy val system: ActorSystem = ActorSystem("SimpleSchedulerSystem")

  private lazy val schedulerActor: ActorRef = system.actorOf(Props(
    new SchedulerActor() with SimpleSchedulerExecutionContext
  ), "SimpleScheduler")

  def scheduleJob(cronExpression: CronExpression, job: Job): Unit = schedulerActor ! ScheduleJob(job, cronExpression)

  def unscheduleJob(jobName: String): Unit = schedulerActor ! UnscheduleJob(jobName)

  def start(): Unit = schedulerActor ! Start

  def stop(): Unit = schedulerActor ! Stop

}

class SchedulerActor extends Actor {
  this: SimpleSchedulerExecutionContext =>

  import ThreadPoolContext._

  private var router: Router = Router(BroadcastRoutingLogic())
  private val jobRunners: mutable.Map[String, ActorRef] = mutable.Map()

  private var tickCancellable: Cancellable = new Cancellable {
    def cancel(): Boolean = true

    def isCancelled: Boolean = true
  }

  override def receive: Receive = {

    case Start =>
      if (tickCancellable.isCancelled) {
        tickCancellable = context.system.scheduler.schedule(tickInterval milliseconds, tickInterval milliseconds)(
          self ! Tick(System.currentTimeMillis())
        )
      }

    case Stop => tickCancellable.cancel()

    case Tick(tick) =>
      router.route(new DateTime(tick), self)

    case ScheduleJob(job, cronExp) =>
      val actorRef: ActorRef = context.actorOf(Props(new TriggerJobRunnerActor(cronExp, job)))
      router = router.addRoutee(actorRef)
      jobRunners(job.name) = actorRef

    case UnscheduleJob(jobName) =>
      val actorRef: ActorRef = jobRunners(jobName)
      router = router.removeRoutee(actorRef)
      context.stop(actorRef)
      jobRunners.remove(jobName)
  }
}

class TriggerJobRunnerActor(cronExpression: CronExpression, job: Job) extends Actor with SimpleSchedulerExecutionContext {

  import ThreadPoolContext._

  val jobRunner: ActorRef = context.actorOf(Props[JobRunnerActor])
  val scheduler: Scheduler = context.system.scheduler

  def receive: Receive = {
    case tick: DateTime if cronExpression.at(tick) =>
      context.become(ignore)
      jobRunner ! (tick -> job)
      scheduler.scheduleOnce(SimpleScheduler.tickInterval + 100 milliseconds) {
        self ! "unbecome"
      }
  }

  def ignore: Receive = {
    case "unbecome" => context.unbecome
    case _ =>
  }


  override def postStop(): Unit = {
    context.stop(jobRunner)
    super.postStop()
  }
}

class JobRunnerActor extends Actor {
  val logger: DiagnosticLoggingAdapter = Logging.getLogger(this)

  override def receive: Receive = {
    case (dateTime: DateTime, job: Job) =>
      logger.info(s"Running Job: '${job.name}' at: $dateTime")
      try {
        job.runnable.run()
      } catch {
        case t: Throwable => logger.error(t, t.getMessage)
      }
  }
}


case class Tick(tick: Long)


case class Job(name: String, runnable: Runnable)

case class ScheduleJob(job: Job, cronExpression: CronExpression)

case class UnscheduleJob(jobName: String)

case object Start

case object Stop