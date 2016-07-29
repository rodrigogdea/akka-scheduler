import java.util

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class CronExpressionSpec extends FlatSpec with Matchers {

  "A CronExpression" should "pop values in last-in-first-out order" in {

    CronExpression("* 13 * * * * *").at(new DateTime(2016, 11, 12, 12, 13, 14, 123)) should be(true)
  }

  //  it should "throw NoSuchElementException if an empty stack is popped" in {
  //    val emptyStack = new util.Stack[Int]()
  //    a [NoSuchElementException] should be thrownBy {
  //      emptyStack.pop()
  //    }
  //  }
}

case class CronExpression(expression: String) {

  def at(dateTime: DateTime): Boolean = true
}