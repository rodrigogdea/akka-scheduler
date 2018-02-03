import org.joda.time.DateTime
import org.scheduler.SimpleScheduler.Job
import org.scheduler.{CronExpression, SimpleScheduler}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.collection.mutable

class SimpleSchedulerSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "The Scheduler" should "schedule a job" in {

    val stack1 = mutable.Stack[String]()
    val stack2 = mutable.Stack[String]()
    val stackForHeavyTask = mutable.Stack[String]()


    SimpleScheduler.start()
    SimpleScheduler.scheduleJob(CronExpression("* * * * * * *"), new Job("Every second", {
      stack1.push(s"Hi 1 at ${DateTime.now()}")
      if ((stack1.size % 3) == 0) throw new Exception(s"Boom at ${DateTime.now()}")
    }))
    SimpleScheduler.scheduleJob(CronExpression("0/2 * * * * * *"), new Job("Every two seconds",
      stack2.push(s"Hi 2 at ${DateTime.now()}")
    ))

    SimpleScheduler.scheduleJob(CronExpression("0/2 * * * * * *"), new Job("Heavy tasks", {
      Thread.sleep(4000)
      stackForHeavyTask.push(s"Finish heavy task at ${DateTime.now()}")
    }))

    Thread.sleep(7000)
    SimpleScheduler.unscheduleJob("Every second")
    Thread.sleep(7000)
    SimpleScheduler.stop()

    stack1.foreach(println)
    stack2.foreach(println)
    stackForHeavyTask.foreach(println)

    stack1.size should equal(6)
    stack2.size should (equal(6) or equal(7))
    stackForHeavyTask.size should (be >= 4 and be <= 6)
  }

}

