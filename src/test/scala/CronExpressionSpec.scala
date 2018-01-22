import org.joda.time.DateTime
import org.rgdea.CronExpression
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

  it should "'* * * * 2 * *' match on Tuesday" in {
    CronExpression("* * * * 2 * *").at(new DateTime(2016, 11, 1, 12, 3, 13, 123)) should be(true)
  }

  it should "'* * * * * 11 *' match at 11th month of year" in {
    CronExpression("* * * * * 11 *").at(new DateTime(2016, 11, 12, 12, 3, 13, 123)) should be(true)
  }

  it should "'* * * * * * 2016' match to year 2016" in {
    CronExpression("* * * * * * 2016").at(new DateTime(2016, 11, 12, 12, 3, 13, 123)) should be(true)
  }

  it should "'0/3 * * * * * 2016' match to year 2016 and stepped seconds" in {
    CronExpression("0/3 * * * * * 2016").at(new DateTime(2016, 11, 12, 12, 3, 9, 123)) should be(true)
  }

  it should "'10/3 * * * * * 2016' match to year 2016 and stepped seconds" in {
    CronExpression("10/3 * * * * * 2016").at(new DateTime(2016, 11, 12, 12, 3, 9, 123)) should be(false)
  }

  it should "'* 13,14,15 * * * * *' match at 14th minute of the hour" in {
    CronExpression("* 13,14,15 * * * * *").at(new DateTime(2016, 11, 12, 12, 14, 2, 123)) should be(true)
  }

  it should "'* 13-15 * * * * *' match at 14th minute of the hour in range" in {
    CronExpression("* 13-15 * * * * *").at(new DateTime(2016, 11, 12, 12, 14, 2, 123)) should be(true)
  }

  it should "'10-30/2 13-15 * * * * *' match at 20th second of minute" in {
    CronExpression("10-30/2 13-15 * * * * *").at(new DateTime(2016, 11, 12, 12, 14, 20, 123)) should be(true)
  }

  it should "'10-30/2 13-15 * * * * *' not match at 34th second of minute" in {
    CronExpression("10-30/2 13-15 * * * * *").at(new DateTime(2016, 11, 12, 12, 14, 34, 123)) should be(false)
  }

  val expression: CronExpression = CronExpression("0 0 3 * * * *")

  it should "'10-30/2 13-15 * * * * *' test time 1" in {
    expression.at(new DateTime(2016, 11, 12, 12, 14, 34, 123)) should be(false)
  }

  it should "'10-30/2 13-15 * * * * *' test time 2" in {
    expression.at(new DateTime(2016, 2, 3, 12, 14, 34, 123)) should be(false)
  }

  it should "'10-30/2 13-15 * * * * *' test time 3" in {
    expression.at(new DateTime(2016, 4, 2, 12, 14, 34, 123)) should be(false)
  }

  it should "'10-30/2 13-15 * * * * *' test time 4" in {
    expression.at(new DateTime(2016, 1, 2, 12, 14, 34, 123)) should be(false)
  }

  it should "throw Exception if try to build '* X * * * * 2016'" in {
    a[Exception] should be thrownBy {
      CronExpression("* X * * * * 2016")
    }
  }

  it should "Convert a DateTime to CronExpression" in {
    val dateTime = new DateTime(2001, 3, 21, 14, 25, 56)
    val expression1 = CronExpression.fromDateTime(dateTime)
    expression1.expression should be("56 25 14 21 * 3 2001")
    expression1.at(dateTime) should be(true)
  }

  it should "Create CronExpression every one hour from dateTome" in {
    val dateTime = new DateTime(2001, 3, 21, 14, 25, 56)
    val expression1 = CronExpression.everyHourFrom(dateTime)
    expression1.expression should be("* 25 * * * * *")
    expression1.at(new DateTime(2001, 3, 21, 15, 25, 2)) should be(true)
    expression1.at(new DateTime(2001, 3, 21, 16, 25, 21)) should be(true)
    expression1.at(new DateTime(2001, 3, 21, 16, 26, 3)) should be(false)
  }

  it should "Create CronExpression every stepped hour from dateTome" in {
    val dateTime = new DateTime(2001, 3, 21, 14, 25, 56)
    val expression1 = CronExpression.everySteppedHourFrom(dateTime, 12)
    expression1.expression should be("* 25 14/12 * * * *")
    expression1.at(new DateTime(2001, 3, 21, 14, 25, 2)) should be(true)
    expression1.at(new DateTime(2001, 3, 22, 2, 25, 21)) should be(true)
    expression1.at(new DateTime(2001, 3, 22, 14, 26, 3)) should be(false)
  }


}


