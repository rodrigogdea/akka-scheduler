import org.junit.Test;

public class SchedulerTest {

    @Test
    public void fromJava() throws InterruptedException {

        CronExpression cronExpression = new CronExpression("* * * * * * *");

        Scheduler.addJob(cronExpression, new Job("A Job!", () -> System.out.println("HOLA desde run....")));

        Scheduler.start();

        Thread.sleep(3000);
    }
}
