package org.rgdea

import org.joda.time.DateTime

object CronExpression {

  def fromDateTime(dateTime: DateTime): CronExpression = {
    val sm = dateTime.getSecondOfMinute.toString
    val mh = dateTime.getMinuteOfHour.toString
    val hd = dateTime.getHourOfDay.toString
    val dm = dateTime.getDayOfMonth.toString
    val my = dateTime.getMonthOfYear.toString
    val yy = dateTime.getYear.toString
    CronExpression(s"$sm $mh $hd $dm * $my $yy")
  }


}

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
      CronItem(toCronValues(intToString(0), 0, 59), dt => dt.getSecondOfMinute),
      CronItem(toCronValues(intToString(1), 0, 59), dt => dt.getMinuteOfHour),
      CronItem(toCronValues(intToString(2), 0, 23), dt => dt.getHourOfDay),
      CronItem(toCronValues(intToString(3), 1, 31), dt => dt.getDayOfMonth),
      CronItem(toCronValues(intToString(4), 1, 7), dt => dt.getDayOfWeek),
      CronItem(toCronValues(intToString(5), 1, 12), dt => dt.getMonthOfYear),
      CronItem(toCronValues(intToString(6), 1), dt => dt.getYear)
    )
      .reduce((mL, mR) => dt => mL(dt) && mR(dt))
  }

  private def toCronValues(cronItemExp: String,
                           minValue: Int,
                           maxValue: Int = Int.MaxValue): CronValues = {
    if (cronItemExp == "*")
      RangeCronValues(Range.inclusive(minValue, maxValue))
    else if (cronItemExp.matches("\\d+/\\d+")) {
      cronItemExp.split("/").map(_.toInt) match {
        case Array(min, step) => RangeCronValues(Range.inclusive(min, maxValue, step))
      }
    }
    else if (cronItemExp.matches("\\d+(,\\d+)*")) {
      ListCronValues(cronItemExp.split(",").map(_.toInt).toList)
    }
    else if (cronItemExp.matches("\\d+-\\d+(/\\d+)?")) {
      cronItemExp.split("/") match {
        case Array(rangeSpec) => rangeSpec.split("-").map(_.toInt) match {
          case Array(min, max) => RangeCronValues(Range.inclusive(min, max))
        }
        case Array(rangeSpec, step) => rangeSpec.split("-").map(_.toInt) match {
          case Array(min, max) => RangeCronValues(Range.inclusive(min, max, step.toInt))
        }
      }
    }
    else
      throw new Exception(s"Expression $cronItemExp not allowed.")
  }
}


case class CronItem(cronValues: CronValues, instant: DateTime => Int)
  extends Function[DateTime, Boolean] {
  def apply(v1: DateTime): Boolean = cronValues.contains(instant(v1))
}

trait CronValues {
  def contains(instant: Int): Boolean
}

case class RangeCronValues(range: Range) extends CronValues {
  def contains(instant: Int): Boolean = range.contains(instant)
}

case class ListCronValues(list: List[Int]) extends CronValues {
  def contains(instant: Int): Boolean = list.contains(instant)
}