package com.heroku.opex.metrics;

import com.codahale.metrics.*;
import org.json.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Reporter extends ScheduledReporter {
    private final String metricsURL = "HEROKU_METRICS_URL";
    private final MetricRegistry metricRegistry;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();

    protected Reporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);

        this.metricRegistry = registry;
    }

    /**
     * Returns a new {@link Builder} for {@link Reporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link Reporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link Reporter} instances. Defaults to using the default locale and
     * time zone, writing to {@code System.out}, converting rates to events/second, converting
     * durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private final String name;
        private Clock clock;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.name = "heroku-jvm-metrics";
            this.clock = Clock.defaultClock();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            disabledMetricAttributes = Collections.emptySet();
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         * See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a {@link Reporter} with the given properties.
         *
         * @return a {@link Reporter}
         */
        public Reporter build() {
            return new Reporter(registry,
                    name,
                    filter,
                    rateUnit,
                    durationUnit);
        }
    }


    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        JSONObject framedMetricData = frameMetrics(gauges, counters);

        RequestBody body = RequestBody.create(JSON, framedMetricData.toString());
        Request request = new Request.Builder()
                .url(this.metricsURL)
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject frameMetrics(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters) {
        JSONObject result = new JSONObject();

        result.put("gauges", gauges);
        result.put("counters", counters);

        return result;
    }
}
