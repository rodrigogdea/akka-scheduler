import org.junit.Assert;
import org.junit.Test;
import org.rgdea.CronExpression;
import org.rgdea.Job;
import org.rgdea.SimpleScheduler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSchedulerTest {

    @Test
    public void fromJava() throws InterruptedException {

        final ConcurrentHashMap<String, AtomicInteger> hashMap = new ConcurrentHashMap<>();
        hashMap.put("Job1", new AtomicInteger(0));
        hashMap.put("Job2", new AtomicInteger(0));

        CronExpression cronExpression = new CronExpression("* * * * * * *"); // Every one second

        SimpleScheduler.scheduleJob(cronExpression, new Job("Job1", () -> hashMap.get("Job1").incrementAndGet() ));
        SimpleScheduler.scheduleJob(cronExpression, new Job("Job2", () -> hashMap.get("Job2").incrementAndGet() ));

        SimpleScheduler.start();

        Thread.sleep(3000);

        Assert.assertEquals(3, hashMap.get("Job1").get());
        Assert.assertEquals(3, hashMap.get("Job2").get());

        SimpleScheduler.unscheduleJob("Job1");

        Thread.sleep(2000);
        Assert.assertEquals(3, hashMap.get("Job1").get());
        Assert.assertEquals(5, hashMap.get("Job2").get());

        SimpleScheduler.stop();
    }
}
