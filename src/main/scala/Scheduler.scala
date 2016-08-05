import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.event.{DiagnosticLoggingAdapter, Logging}
import akka.routing.{BroadcastRoutingLogic, Routee, Router}
import org.joda.time.DateTime

import scala.collection.mutable
import scala.concurrent.duration._

trait SchedulerExecutionContext {
  import scala.concurrent.ExecutionContext
  implicit val ec = ExecutionContext.fromExecutor(null)
}

object Scheduler extends SchedulerExecutionContext {
  val tickInterval: Int = 500

  private lazy val system: ActorSystem = ActorSystem("SchedulerSystem")

  private lazy val schedulerActor: ActorRef = system.actorOf(Props[SchedulerActor], "Scheduler")
  private val cancellable: Cancellable = system.scheduler.schedule(tickInterval milliseconds, tickInterval milliseconds)(
    schedulerActor ! Tick(System.currentTimeMillis())
  )

  def addJob(cronExpression: CronExpression, job: Job) = schedulerActor ! (cronExpression -> job)

  def removeJob(jobName: String) = schedulerActor ! ("remove" -> jobName)

}

class SchedulerActor extends Actor {

  private var router: Router = Router(BroadcastRoutingLogic())
  private val jobRunners: mutable.Map[String, ActorRef] = mutable.Map()

  override def receive: Receive = {
    case Tick(tick) =>
      router.route(new DateTime(tick), self)

    case (cronExp: CronExpression, job: Job) =>
      val actorRef: ActorRef = context.actorOf(Props(new TriggerJobRunnerActor(cronExp, job)))
      router = router.addRoutee(actorRef)
      jobRunners(job.name) = actorRef

    case ("remove", jobName: String) =>
      val actorRef: ActorRef = jobRunners(jobName)
      router.removeRoutee(actorRef)
      context.stop(actorRef)
      jobRunners.remove(jobName)
  }
}

class TriggerJobRunnerActor(cronExpression: CronExpression, job: Job) extends Actor with SchedulerExecutionContext {

  val jobRunner: ActorRef = context.actorOf(Props[JobRunnerActor])

  def receive: Receive = {
    case tick: DateTime => if (cronExpression.at(tick)) {
      context.become(ignore)
      jobRunner ! (tick -> job)
      context.system.scheduler.scheduleOnce(Scheduler.tickInterval + 100 milliseconds)(context.unbecome())
    }
  }

  def ignore: Receive = {
    case _ =>
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