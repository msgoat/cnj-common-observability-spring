package group.msg.at.cloud.common.observability.metrics.undertow;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import io.undertow.Undertow;
import io.undertow.server.handlers.MetricsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * Binds all Undertow metrics to Micrometer's {@code MeterRegistry} during application startup.
 * <p>
 * TODO: check if this approach can be replaced with proper integration into autoconfiguration
 * </p>
 */
@Component
@ConditionalOnClass(Undertow.class)
public class UndertowMeterBinder implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UndertowMetricsHandlerWrapper undertowMetricsHandlerWrapper;

    public UndertowMeterBinder(UndertowMetricsHandlerWrapper undertowMetricsHandlerWrapper) {
        this.undertowMetricsHandlerWrapper = undertowMetricsHandlerWrapper;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        bindTo(applicationReadyEvent.getApplicationContext().getBean(MeterRegistry.class));
    }

    public void bindTo(MeterRegistry meterRegistry) {
        logger.info("*** METRICS *** Binding Undertow metrics handler wrapper to Micrometer meter registry");
        bind(meterRegistry, undertowMetricsHandlerWrapper.getMetricsHandler());
        logger.info("*** METRICS *** Binding JBoss Thread Pool metrics to Micrometer meter registry");
        new JbossThreadPoolMetrics(new ArrayList<>()).bindTo(meterRegistry);
    }

    public void bind(MeterRegistry registry, MetricsHandler metricsHandler) {
        bindTimer(registry, "undertow.requests", "Number of requests", metricsHandler,
                m -> m.getMetrics().getTotalRequests(), m2 -> m2.getMetrics().getMinRequestTime());
        bindTimeGauge(registry, "undertow.request.time.max", "The longest request duration in time", metricsHandler,
                m -> m.getMetrics().getMaxRequestTime());
        bindTimeGauge(registry, "undertow.request.time.min", "The shortest request duration in time", metricsHandler,
                m -> m.getMetrics().getMinRequestTime());
        bindCounter(registry, "undertow.request.errors", "Total number of error requests ", metricsHandler,
                m -> m.getMetrics().getTotalErrors());

    }

    private void bindTimer(MeterRegistry registry, String name, String desc, MetricsHandler metricsHandler,
                           ToLongFunction<MetricsHandler> countFunc, ToDoubleFunction<MetricsHandler> consumer) {
        FunctionTimer.builder(name, metricsHandler, countFunc, consumer, TimeUnit.MILLISECONDS)
                .description(desc).register(registry);
    }

    private void bindTimeGauge(MeterRegistry registry, String name, String desc, MetricsHandler metricResult,
                               ToDoubleFunction<MetricsHandler> consumer) {
        TimeGauge.builder(name, metricResult, TimeUnit.MILLISECONDS, consumer).description(desc)
                .register(registry);
    }

    private void bindCounter(MeterRegistry registry, String name, String desc, MetricsHandler metricsHandler,
                             ToDoubleFunction<MetricsHandler> consumer) {
        FunctionCounter.builder(name, metricsHandler, consumer).description(desc)
                .register(registry);
    }
}