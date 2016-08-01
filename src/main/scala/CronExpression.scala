import org.joda.time.DateTime

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
    if (cronItemExp == "*") CronItem(None)
    else if (cronItemExp.matches("\\d+/\\d+")) {
      val startStep: Array[String] = cronItemExp.split("/")
      CronItem(Some(startStep.head.toInt), Some(startStep.tail.head.toInt))
    }
    else
      CronItem(Some(cronItemExp.toInt))
  }
}

case class CronItem(start: Option[Int], step: Option[Int] = None, range: Option[Range] = None)

trait CronItemMatcher extends Function[DateTime, Boolean] {
  def item: CronItem
}

case class SecondsMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getSecondOfMinute)
}

case class MinutesMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getMinuteOfHour)
}

case class HoursMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getHourOfDay)
}

case class DaysMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getDayOfMonth)
}

case class DayOfWeekMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getDayOfWeek)
}

case class MonthMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getMonthOfYear)
}

case class YearMatcher(item: CronItem) extends CronItemMatcher {
  def apply(dateTime: DateTime) = item.start.forall(_ == dateTime.getYear)
}
