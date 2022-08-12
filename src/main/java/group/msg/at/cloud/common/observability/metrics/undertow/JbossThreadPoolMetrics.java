package group.msg.at.cloud.common.observability.metrics.undertow;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;
import io.micrometer.core.lang.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

/**
 * Custom {@code MeterBinder} which exports metrics of Undertow threadpool resources
 * using metrics name prefix {@code undertow.threads.*}.
 */
@NonNullApi
@NonNullFields
public class JbossThreadPoolMetrics implements MeterBinder, AutoCloseable {

    private static final Logger log = LogManager.getLogger(JbossThreadPoolMetrics.class);
    private static final String JMX_DOMAIN_STANDALONE = "jboss.threads";
    private static final String METRIC_NAME_PREFIX = "undertow.threads.";

    private final MBeanServer mBeanServer;
    private final Iterable<Tag> tags;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public JbossThreadPoolMetrics(Iterable<Tag> tags) {
        this(getMBeanServer(), tags);
    }

    public JbossThreadPoolMetrics(MBeanServer mBeanServer, Iterable<Tag> tags) {
        this.tags = tags;
        this.mBeanServer = mBeanServer;
    }

    public static MBeanServer getMBeanServer() {
        List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
        if (!mBeanServers.isEmpty()) {
            return mBeanServers.get(0);
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        registerConnectionPoolMetrics(registry);
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private void registerConnectionPoolMetrics(MeterRegistry registry) {
        registerMetricsEventually(":type=thread-pool,name=*",
                (name, allTags) -> {
                    registerGaugeForObject(registry, name,
                            "CorePoolSize", "core.pool.size", allTags,
                            "The size below which new threads will always be created if no idle threads are available", null);
                    registerGaugeForObject(registry, name,
                            "MaximumPoolSize", "max.pool.size", allTags,
                            "The maximum pool size", null);
                    registerGaugeForObject(registry, name,
                            "PoolSize", "current.pool.size", allTags,
                            "An estimate of the current number of active threads in the pool", null);
                    registerGaugeForObject(registry, name,
                            "LargestPoolSize", "largest.pool.size", allTags,
                            "An estimate of the peak number of threads that the pool has ever held", null);
                    registerGaugeForObject(registry, name,
                            "ActiveCount", "active.count", allTags,
                            "An estimate of the current number of active (busy) threads", null);
                    registerGaugeForObject(registry, name,
                            "KeepAliveTimeSeconds", "keep.alive.time.seconds", allTags,
                            "The thread keep-alive time in seconds", null);
                    registerGaugeForObject(registry, name,
                            "MaximumQueueSize", "max.queue.size", allTags,
                            "The maximum queue size for this thread pool", null);
                    registerGaugeForObject(registry, name,
                            "QueueSize", "current.queue.size", allTags,
                            "An estimate of the current queue size", null);
                    registerGaugeForObject(registry, name,
                            "LargestQueueSize", "largest.queue.size", allTags,
                            "An estimate of the peak size of the queue", null);
                    registerGaugeForObject(registry, name,
                            "SubmittedTaskCount", "submitted.tasks.total", allTags,
                            "An estimate of the total number of tasks ever submitted to this thread pool", null);
                    registerGaugeForObject(registry, name,
                            "RejectedTaskCount", "rejected.tasks.total", allTags,
                            "An estimate of the total number of tasks ever rejected by this thread pool for any reason", null);
                    registerGaugeForObject(registry, name,
                            "CompletedTaskCount", "completed.tasks.total", allTags,
                            "An estimate of the total number of tasks completed by this thread pool", null);
                });
    }

    /**
     * If the Undertow MBean already exists, register metrics immediately. Otherwise, register an MBean registration listener
     * with the MBeanServer and register metrics when/if the MBeans becomes available.
     */
    private void registerMetricsEventually(String namePatternSuffix, BiConsumer<ObjectName, Tags> perObject) {
        Set<ObjectName> objectNames = this.mBeanServer.queryNames(getNamePattern(namePatternSuffix), null);
        log.info("*** METRICS *** Found [{}] JMX object names: [{}]", objectNames.size(), objectNames);
        if (!objectNames.isEmpty()) {
            // MBeans are present, so we can register metrics now.
            objectNames.forEach(objectName -> perObject.accept(objectName, Tags.concat(tags, nameTag(objectName))));
            return;
        }

        registerMetricsAsynchronously(this.mBeanServer, getNamePattern(namePatternSuffix), perObject);
    }

    private void registerMetricsAsynchronously(MBeanServer server, ObjectName namePattern, BiConsumer<ObjectName, Tags> perObject) {
        executor.submit(() -> {
            int retries = 0;
            Set<ObjectName> objectNames = server.queryNames(namePattern, null);
            while (objectNames.isEmpty()) {
                retries++;
                try {
                    Thread.sleep(5000);
                    objectNames = server.queryNames(namePattern, null);
                } catch (InterruptedException e) {
                    log.info("*** METRICS *** Asynchronous registration of metrics interrupted after [{}] retries; will terminate", retries);
                }
            }
            if (!objectNames.isEmpty()) {
                log.info("*** METRICS *** Found [{}] JMX object names after [{}] retries: [{}]", objectNames.size(), retries, objectNames);
                objectNames.forEach(objectName -> perObject.accept(objectName, Tags.concat(tags, nameTag(objectName))));
            }
        });
    }

    private ObjectName getNamePattern(String namePatternSuffix) {
        try {
            return new ObjectName(JMX_DOMAIN_STANDALONE + namePatternSuffix);
        } catch (MalformedObjectNameException e) {
            // should never happen
            throw new RuntimeException("Error registering Tomcat JMX based metrics", e);
        }
    }

    private boolean hasObjectName(String name) {
        try {
            return this.mBeanServer.queryNames(new ObjectName(name), null).size() == 1;
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        }
    }

    private long safeLong(Callable<Object> callable) {
        try {
            return Long.parseLong(callable.call().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private Iterable<Tag> nameTag(ObjectName name) {
        Iterable<Tag> result = Collections.emptyList();
        String nameTagValue = name.getKeyProperty("name");
        if (nameTagValue != null) {
            result = Tags.of("name", nameTagValue.replaceAll("\"", ""));
        }
        return result;
    }

    private void registerGaugeForObject(
            MeterRegistry registry,
            ObjectName o,
            String jmxMetricName,
            String meterName,
            Tags allTags,
            String description,
            @Nullable String baseUnit) {
        final AtomicReference<Gauge> gauge = new AtomicReference<>();
        gauge.set(Gauge
                .builder(
                        METRIC_NAME_PREFIX + meterName,
                        mBeanServer,
                        getJmxAttribute(registry, gauge, o, jmxMetricName)
                )
                .description(description)
                .baseUnit(baseUnit)
                .tags(allTags)
                .register(registry)
        );
    }

    private void registerFunctionCounterForObject(MeterRegistry registry, ObjectName o, String jmxMetricName, String meterName, Tags allTags, String description, @Nullable String baseUnit) {
        final AtomicReference<FunctionCounter> counter = new AtomicReference<>();
        counter.set(FunctionCounter
                .builder(
                        METRIC_NAME_PREFIX + meterName,
                        mBeanServer,
                        getJmxAttribute(registry, counter, o, jmxMetricName)
                )
                .description(description)
                .baseUnit(baseUnit)
                .tags(allTags)
                .register(registry)
        );
    }

    private void registerTimeGaugeForObject(MeterRegistry registry, ObjectName o, String jmxMetricName,
                                            String meterName, Tags allTags, String description) {
        final AtomicReference<TimeGauge> timeGauge = new AtomicReference<>();
        timeGauge.set(TimeGauge
                .builder(
                        METRIC_NAME_PREFIX + meterName,
                        mBeanServer,
                        TimeUnit.MILLISECONDS,
                        getJmxAttribute(registry, timeGauge, o, jmxMetricName)
                )
                .description(description)
                .tags(allTags)
                .register(registry)
        );
    }

    private ToDoubleFunction<MBeanServer> getJmxAttribute(
            MeterRegistry registry,
            AtomicReference<? extends Meter> meter,
            ObjectName o,
            String jmxMetricName) {
        return s -> safeDouble(
                () -> {
                    if (!s.isRegistered(o)) {
                        registry.remove(meter.get());
                    }
                    return s.getAttribute(o, jmxMetricName);
                });
    }

    private double safeDouble(Callable<Object> callable) {
        try {
            return Double.parseDouble(callable.call().toString());
        } catch (Exception e) {
            log.error("could not parse double value: {}", e.getMessage());
            return Double.NaN;
        }
    }
}
