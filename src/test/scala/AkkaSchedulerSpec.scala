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

      Scheduler.addJob(CronExpression("* * * * * * *"), Job("Un job uno", new Runnable {
        def run(): Unit = println("Heavy job one")
      }))

      Scheduler.addJob(CronExpression("* * * * 9 * *"), Job("Un job dos", new Runnable {
        def run(): Unit = println("Heavy job two")
      }))

      Thread.sleep(10000)

      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

  }

}

