import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.actor.Actor.Receive
import akka.routing.{BroadcastRoutingLogic, Router}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AkkaSchedulerSpec extends TestKit(ActorSystem("AkkaSchedulerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "send back messages unchanged" in {

      Schedurler.addJob("HOLA")
      Schedurler.addJob("KPO")

      Thread.sleep(1500)

      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

  }

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

object Schedurler {
  private lazy val system: ActorSystem = ActorSystem("SchedulerSystem")

  private lazy val schedulerActor: ActorRef = system.actorOf(Props[SchedulerActor], "Scheduler")

  private val cancellable: Cancellable = system.scheduler.schedule(500 milliseconds, 500 milliseconds, new Runnable {
    override def run(): Unit = schedulerActor ! Tick(System.currentTimeMillis())
  })

  def addJob(name: String) = schedulerActor ! name

}