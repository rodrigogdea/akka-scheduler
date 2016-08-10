import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

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

      Scheduler.start()

      Thread.sleep(10000)

      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

  }

}

