import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.actor.Actor.Receive
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

      val actorOf: ActorRef = system.actorOf(Props[Scheduler], "Scheduler")

      Thread.sleep(500)

      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

  }

}

class Scheduler extends Actor {

  context.system.scheduler.schedule(2 milliseconds, 50 milliseconds, new Runnable {
    override def run(): Unit = self ! Tick(System.currentTimeMillis())
  })

  override def receive: Receive = {
    case Tick(tick) => println(tick)
  }
}

case class Tick(tick: Long)