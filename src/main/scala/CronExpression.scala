import org.joda.time.DateTime


/**
  * Pattern: "SM MH HD DM DW MY YY"
  * SM: Second of minute
  * MH: Minute of the hour
  * HD: Hour of the day
  * DM: Day of the Month
  * DW: Day of the week
  * MY: Month of the year
  * YY: Year
  *
  * Each Cron Item can take:
  * "*": Match any value of a given instance
  * "\d+(,\d+)*": Match one value or a list of values
  * "\d+/\d+": Match a starting value and a step
  * "\d+-\d+": Match a range
  *
  * Possible values depends on the domain of the particular cron item (eg: to Seconds will be 0-59)
  *
  * Examples:
  * "* * * * * * *" -> Every second
  * "0 * * * * * *" -> Each 0 second of every minute
  * "* 0 * * * * *" -> Every second at minute 0 of every hour
  *
  *
  *
  * @param expression The string expression of the Cron
  */
case class CronExpression(expression: String) {

  private val matcher = parse(expression)

  def at(dateTime: DateTime) = matcher(dateTime)

  private def parse(expression: String): Function[DateTime, Boolean] = {
    val intToString: Map[Int, String] = expression.trim.split(" ").zipWithIndex.map(_.swap).toMap
    List[Function[DateTime, Boolean]](
      SecondsMatcher(toCronItem(intToString(0))),
      MinutesMatcher(toCronItem(intToString(1))),
      HoursMatcher(toCronItem(intToString(2))),
      DaysMatcher(toCronItem(intToString(3))),
      DayOfWeekMatcher(toCronItem(intToString(4))),
      MonthMatcher(toCronItem(intToString(5))),
      YearMatcher(toCronItem(intToString(6)))
    )
      .reduce((mL, mR) => dt => mL(dt) && mR(dt))
  }

  private def toCronItem(cronItemExp: String): CronItem = {
    if (cronItemExp == "*") CronItem(List())
    else if (cronItemExp.matches("\\d+/\\d+")) {
      val startStep = cronItemExp.split("/").map(_.toInt)
      CronItem(List(startStep.head), Some(startStep.tail.head))
    }
    else if (cronItemExp.matches("\\d+(,\\d+)*")) {
      CronItem(cronItemExp.split(",").map(_.toInt).toList)
    }
    else
      throw new Exception(s"Expression $cronItemExp not allowed.")
  }
}

case class CronItem(start: List[Int], step: Option[Int] = None, range: Option[Range] = None)

trait CronItemMatcher extends Function[DateTime, Boolean] {
  def item: CronItem

  protected def eval(instant: Int): Boolean = {
    item.start.isEmpty ||
      item.start.contains(instant) ||
      item.step.exists(item.start.forall(_ <= instant) && instant % _ == 0)
  }
}

case class SecondsMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getSecondOfMinute)
}

case class MinutesMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getMinuteOfHour)
}

case class HoursMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getHourOfDay)
}

case class DaysMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getDayOfMonth)
}

case class DayOfWeekMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getDayOfWeek)
}

case class MonthMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getMonthOfYear)
}

case class YearMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = eval(dateTime.getYear)
}
