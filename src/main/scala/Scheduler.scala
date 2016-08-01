import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.routing.{BroadcastRoutingLogic, Routee, Router}
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


object Scheduler {
  private lazy val system: ActorSystem = ActorSystem("SchedulerSystem")

  private lazy val schedulerActor: ActorRef = system.actorOf(Props[SchedulerActor], "Scheduler")

  private val cancellable: Cancellable = system.scheduler.schedule(500 milliseconds, 500 milliseconds)(
    schedulerActor ! Tick(System.currentTimeMillis())
  )

  def addJob(cronExpression: CronExpression, job: Job) = schedulerActor ! (cronExpression -> job)

}

class SchedulerActor extends Actor {

  private var router: Router = Router(BroadcastRoutingLogic())

  override def receive: Receive = {
    case Tick(tick) => router.route(new DateTime(tick), self)

    case (cronExp: CronExpression, job: Job) =>
      val actorRef: ActorRef = context.actorOf(Props(new TriggerJobRunnerActor(cronExp, job)))
      router = router.addRoutee(actorRef)
  }
}

class TriggerJobRunnerActor(cronExpression: CronExpression, job: Job) extends Actor {

  val jobRunner: ActorRef = context.actorOf(Props[JobRunnerActor])

  def receive: Receive = {
    case tick: DateTime => if (cronExpression.at(tick)) {
      context.become(ignore)
      jobRunner ! (tick -> job)
      context.system.scheduler.scheduleOnce(600 milliseconds)(context.unbecome())
    }
  }

  def ignore: Receive = {
    case _ =>
  }

}

class JobRunnerActor extends Actor {

  override def receive: Receive = {
    case (dateTime:DateTime, job: Job) => {
      println(s"Running Job: '${job.name}' at: ${dateTime}")
      job.runnable.run()
    }
  }
}


case class Tick(tick: Long)


case class Job(name: String, runnable: Runnable)