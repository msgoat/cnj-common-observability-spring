package group.msg.at.cloud.common.observability.metrics.undertow;

import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code Configuration} which adds an {@code UndertowDeploymentInfoCustomizer} bean to the application context
 * actually binding the metrics handler to the server configuration.
 */
@Configuration
@ConditionalOnClass(Undertow.class)
public class UndertowMetricsConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    UndertowDeploymentInfoCustomizer undertowDeploymentInfoCustomizer(UndertowMetricsHandlerWrapper undertowMetricsHandlerWrapper) {
        logger.info("customize Undertow server configuration with metrics handler wrapper");
        return deploymentInfo ->
                deploymentInfo.addOuterHandlerChainWrapper(undertowMetricsHandlerWrapper);
    }
}
