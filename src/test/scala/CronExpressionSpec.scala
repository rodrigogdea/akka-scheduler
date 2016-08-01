import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class CronExpressionSpec extends FlatSpec with Matchers {

  "A CronExpression" should "'13 * * * * * *' match at 13th second" in {
    CronExpression("13 * * * * * *").at(new DateTime(2016, 11, 12, 12, 3, 13, 123)) should be(true)
  }

  it should "'* 13 * * * * *' match at 13th minute" in {
    CronExpression("* 13 * * * * *").at(new DateTime(2016, 11, 12, 12, 13, 2, 123)) should be(true)
  }

  it should "'* * 13 * * * *' match at 13th hour of the day" in {
    CronExpression("* * 13 * * * *").at(new DateTime(2016, 11, 12, 13, 3, 1, 123)) should be(true)
  }

  it should "'* * * 23 * * *' match at 23th day of the Month" in {
    CronExpression("* * * 23 * * *").at(new DateTime(2016, 11, 23, 12, 3, 13, 123)) should be(true)
  }

  it should "'* * * * 2 * *' match at 2nd day of the week" in {
    CronExpression("* * * * 2 * *").at(new DateTime(2016, 11, 1, 12, 3, 13, 123)) should be(true)
  }

  it should "'* * * * * 11 *' match at 11th month of year" in {
    CronExpression("* * * * * 11 *").at(new DateTime(2016, 11, 12, 12, 3, 13, 123)) should be(true)
  }

  it should "'* * * * * * 2016' match to year 2016" in {
    CronExpression("* * * * * * 2016").at(new DateTime(2016, 11, 12, 12, 3, 13, 123)) should be(true)
  }

  //  it should "throw NoSuchElementException if an empty stack is popped" in {
  //    val emptyStack = new util.Stack[Int]()
  //    a [NoSuchElementException] should be thrownBy {
  //      emptyStack.pop()
  //    }
  //  }
}


