# An other simple Scheduler

A simple scheduler based on Akka.

# Using scheduler

### From Scala
```scala
    val expression = CronExpression("0 0 2-3 * * * *")
    
    SimpleScheduler.scheduleJob(expression, Job("Run every day at 2 and 3 AM", new Runnable {
    def run(): Unit = println("Heavy job one")
    }))
    
    SimpleScheduler.start()
```
### From Java
```java
    CronExpression cronExpression = new CronExpression("* * * * * * *");
    
    SimpleScheduler.scheduleJob(cronExpression, new Job("A Job!", () -> System.out.println("run....")));
    
    SimpleScheduler.start();
```

# Cron Expression

* Pattern: "SM MH HD DM DW MY YY"
<pre>
SM: Second of minute
MH: Minute of the hour
HD: Hour of the day
DM: Day of the Month
DW: Day of the week
MY: Month of the year
YY: Year
</pre>

* Each Cron Item can take:
<pre>
"*": Match any value of a given instance
"\d+(,\d+)*": Match one value or a list of values
"\d+/\d+": Match a starting value and a step
"\d+-\d+(/\d+)": Match a range and optionaly a step inside the range

 Possible values depends on the domain of the particular cron item (eg: to Seconds will be 0-59)
</pre>

* Examples:
<pre>
"* * * * * * *" -> Every second
"0 * * * * * *" -> Each 0 second of every minute
"* 0 * * * * *" -> Every second at minute 0 of every hour
"0 0 13 * 1 * *" -> Every monday at 1 PM
</pre>
