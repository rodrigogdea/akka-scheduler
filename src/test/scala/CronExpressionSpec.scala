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

  it should "throw Exception if try to build '* X * * * * 2016'" in {
    a[Exception] should be thrownBy {
      CronExpression("* X * * * * 2016")
    }
  }
}


