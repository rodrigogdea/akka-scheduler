package org.rgdea

import org.joda.time.DateTime

object CronExpression {

  def fromDateTime(dateTime: DateTime): CronExpression = {
    val (sm, mh, hd, dm, my, yy) = dateTimeValues(dateTime)
    CronExpression(s"$sm $mh $hd $dm * $my $yy")
  }

  def everyHourFrom(dateTime: DateTime): CronExpression = {
    val (_, mh, _, _, _, _) = dateTimeValues(dateTime)
    CronExpression(s"* $mh * * * * *")
  }

  def everySteppedHourFrom(dateTime: DateTime, step: Int): CronExpression = {
    val (_, mh, hd, _, _, _) = dateTimeValues(dateTime)
    CronExpression(s"* $mh $hd/$step * * * *")
  }

  def everyDayFrom(dateTime: DateTime): CronExpression = {
    val (_, mh, hd, _, _, _) = dateTimeValues(dateTime)
    CronExpression(s"* $mh $hd * * * *")
  }


  def dateTimeValues(dateTime: DateTime): (String, String, String, String, String, String) = (
    dateTime.getSecondOfMinute.toString,
    dateTime.getMinuteOfHour.toString,
    dateTime.getHourOfDay.toString,
    dateTime.getDayOfMonth.toString,
    dateTime.getMonthOfYear.toString,
    dateTime.getYear.toString
  )

  private[CronExpression] val startWithStep = "(\\d+)/(\\d+)".r
  private[CronExpression] val listOfValues = "(\\d+)((,\\d+)*)".r
  private[CronExpression] val rangeWithStep = "(\\d+)-(\\d+)(/(\\d+))?".r
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

  import CronExpression._

  private val matcher = {
    val position: Map[Int, String] = expression.trim.split(" ").zipWithIndex.map(_.swap).toMap
    List[Function[DateTime, Boolean]](
      CronItem(toCronValues(position(0), 0, 59), dt => dt.getSecondOfMinute),
      CronItem(toCronValues(position(1), 0, 59), dt => dt.getMinuteOfHour),
      CronItem(toCronValues(position(2), 0, 23), dt => dt.getHourOfDay),
      CronItem(toCronValues(position(3), 1, 31), dt => dt.getDayOfMonth),
      CronItem(toCronValues(position(4), 1, 7), dt => dt.getDayOfWeek),
      CronItem(toCronValues(position(5), 1, 12), dt => dt.getMonthOfYear),
      CronItem(toCronValues(position(6), 1), dt => dt.getYear)
    ).reduce((mL, mR) => dt => mL(dt) && mR(dt))
  }

  def at(dateTime: DateTime): Boolean = matcher(dateTime)

  private def toCronValues(cronItemExp: String,
                           minValue: Int,
                           maxValue: Int = Int.MaxValue): CronValues = {
    cronItemExp match {
      case "*" => RangeCronValues(Range.inclusive(minValue, maxValue))
      case startWithStep(start, step) =>
        ListCronValues(Range.inclusive(start.toInt, start.toInt + maxValue, step.toInt)
          .toList.map(_ % (maxValue + 1)))
      case listOfValues(first, "", _) => ListCronValues(List(first.toInt))
      case listOfValues(first, more, _) => ListCronValues((first + more).split(",").map(_.toInt).toList)
      case rangeWithStep(min, max, null, null) => RangeCronValues(Range.inclusive(min.toInt, max.toInt))
      case rangeWithStep(min, max, _, step) => RangeCronValues(Range.inclusive(min.toInt, max.toInt, step.toInt))
      case _ => throw new Exception(s"Expression $cronItemExp not allowed.")
    }
  }

  case class CronItem(cronValues: CronValues, instant: DateTime => Int) extends Function[DateTime, Boolean] {
    def apply(time: DateTime): Boolean = cronValues.contains(instant(time))
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

}


