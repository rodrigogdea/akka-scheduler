import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.actor.Actor.Receive
import akka.routing.{BroadcastRoutingLogic, Router}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll

class AkkaSchedulerSpec extends TestKit(ActorSystem("AkkaSchedulerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "send back messages unchanged" in {

      Scheduler.addJob("HOLA")
      Scheduler.addJob("KPO")

      Thread.sleep(1500)

      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

  }

}

