package group.msg.at.cloud.common.observability.metrics.undertow;

import io.undertow.Undertow;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.MetricsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * {@code Undertow}-specific handler wrapper to add a {@code MetricsHandler} to the undertow handler chain
 * in order to bind Micrometer to Undertow's metrics system.
 */
@Component
@ConditionalOnClass(Undertow.class)
public class UndertowMetricsHandlerWrapper implements HandlerWrapper {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private MetricsHandler metricsHandler;

    @Override
    public HttpHandler wrap(HttpHandler handler) {
        logger.info("adding handler wrapper to Undertow handler chain");
        metricsHandler = new MetricsHandler(handler);
        return metricsHandler;
    }

    public MetricsHandler getMetricsHandler() {
        if (metricsHandler == null) {
            throw new IllegalStateException("Trying to access Undertow's metrics handler while it's still not set");
        }
        return metricsHandler;
    }
}
