package com.heroku.opex.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.*;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by cdodrill on 2/27/17.
 */
public class Collector {
    public static final String JVM_METRIC_PREFIX = "jvm_";
    private final MetricRegistry metricRegistry;

    public Collector() {
        this.metricRegistry = new MetricRegistry();

        initGCMetrics();

        Reporter reporter = Reporter.forRegistry(this.metricRegistry).build();
        reporter.start(1, TimeUnit.SECONDS);
    }

    private void initGCMetrics() {
        this.metricRegistry.register(
                this.JVM_METRIC_PREFIX + "gc",
                new GarbageCollectorMetricSet(ManagementFactory.getGarbageCollectorMXBeans())
        );
    }
}
