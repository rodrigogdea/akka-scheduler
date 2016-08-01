import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.routing.{BroadcastRoutingLogic, Router}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


object Scheduler {
  private lazy val system: ActorSystem = ActorSystem("SchedulerSystem")

  private lazy val schedulerActor: ActorRef = system.actorOf(Props[SchedulerActor], "Scheduler")

  private val cancellable: Cancellable = system.scheduler.schedule(500 milliseconds, 500 milliseconds, new Runnable {
    override def run(): Unit = schedulerActor ! Tick(System.currentTimeMillis())
  })

  def addJob(name: String) = schedulerActor ! name

}

class SchedulerActor extends Actor {

  private var router: Router = Router(BroadcastRoutingLogic())

  override def receive: Receive = {
    case Tick(tick) => router.route(new Date(tick).toString, self)

    case name: String => router = router.addRoutee(context.actorOf(Props(new JobRunnerActor(name))))
  }
}

class JobRunnerActor(name: String) extends Actor {

  override def receive: Receive = {
    case tick: String => println(s"$name: $tick")
  }
}

case class Tick(tick: Long)

