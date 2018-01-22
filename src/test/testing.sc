import org.joda.time.DateTime
import org.rgdea.CronExpression

Range(1, 10 + 1, 3).contains(5)

Range(0, Int.MaxValue).contains(2017)

CronExpression("* 3 12/3 * * * *")
  .at(new DateTime(2016, 11, 12, 16, 3, 13, 123))

val rangeWithStep = "(\\d+)((,\\d+)*)".r

"2,3,4" match {
  case rangeWithStep(x, "", _) => println(s"Single: $x ")
  case rangeWithStep(x, y, _) => println(s"List: $x ${y.split(",").toList}")
}

12+24

Range.inclusive(12, 36, 7).toList.map(_ % 24)





