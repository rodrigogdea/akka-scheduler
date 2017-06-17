package org.rgdea

import org.joda.time.DateTime


/**
  * <pre>
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
  * </pre>
  *
  * @param expression The string expression of the Cron
  */
case class CronExpression(expression: String) {

  private val matcher = parse(expression)

  def at(dateTime: DateTime): Boolean = matcher(dateTime)

  private def parse(expression: String): Function[DateTime, Boolean] = {
    val intToString: Map[Int, String] = expression.trim.split(" ").zipWithIndex.map(_.swap).toMap
    List[Function[DateTime, Boolean]](
      SecondsMatcher(toCronItem(intToString(0), 59)),
      MinutesMatcher(toCronItem(intToString(1), 59)),
      HoursMatcher(toCronItem(intToString(2), 23)),
      DaysMatcher(toCronItem(intToString(3), 31)),
      DayOfWeekMatcher(toCronItem(intToString(4), 7)),
      MonthMatcher(toCronItem(intToString(5), 12)),
      YearMatcher(toCronItem(intToString(6), 0))
    )
      .reduce((mL, mR) => dt => mL(dt) && mR(dt))
  }

  private def toCronItem(cronItemExp: String, maxValue: Int): CronItem = {
    if (cronItemExp == "*")
      CronItem(List())
    else if (cronItemExp.matches("\\d+/\\d+")) {
      cronItemExp.split("/").map(_.toInt) match {
        case Array(min, step) => CronItem(Range(min, maxValue + 1, step).toList)
      }
    }
    else if (cronItemExp.matches("\\d+(,\\d+)*")) {
      CronItem(cronItemExp.split(",").map(_.toInt).toList)
    }
    else if (cronItemExp.matches("\\d+-\\d+(/\\d+)?")) {
      cronItemExp.split("/") match {
        case Array(rangeSpec) => rangeSpec.split("-").map(_.toInt) match {
          case Array(min, max) => CronItem(Range(min, max + 1).toList)
        }
        case Array(rangeSpec, step) => rangeSpec.split("-").map(_.toInt) match {
          case Array(min, max) => CronItem(Range(min, max + 1, step.toInt).toList)
        }
      }
    }
    else
      throw new Exception(s"Expression $cronItemExp not allowed.")
  }
}

case class CronItem(start: List[Int])

trait CronItemMatcher extends Function[DateTime, Boolean] {
  def item: CronItem

  protected def eval(instant: Int): Boolean = item.start.isEmpty || item.start.contains(instant)
}

//TODO: Validate all CronItem Domains

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
