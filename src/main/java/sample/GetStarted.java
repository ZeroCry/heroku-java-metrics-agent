package sample;
import com.codahale.metrics.*;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;

import java.util.concurrent.TimeUnit;
import java.lang.management.ManagementFactory;

public class GetStarted {
    static final MetricRegistry metrics = new MetricRegistry();
    public static void main(String args[]) {
        startReport();

        metrics.register(
                 "jvm_gc",
                new GarbageCollectorMetricSet(ManagementFactory.getGarbageCollectorMXBeans())
        );

        wait5Seconds();
    }

    static void startReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);
    }

    static void wait5Seconds() {
        try {
            Thread.sleep(5*1000);
        }
        catch(InterruptedException e) {}
    }
}