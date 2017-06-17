import org.rgdea.{CronExpression, Job, Runnable, SimpleScheduler}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.collection.mutable

class SimpleSchedulerSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "The Scheduler" should "schedule a job" in {

    val stack = mutable.Stack[String]()

    SimpleScheduler.start()

    SimpleScheduler.scheduleJob(CronExpression("* * * * * * *"), Job("Add to stack", Runnable {
      stack.push("Hi")
    }))

    Thread.sleep(3000)
    SimpleScheduler.stop()

    stack.size should be(3)
  }

}

