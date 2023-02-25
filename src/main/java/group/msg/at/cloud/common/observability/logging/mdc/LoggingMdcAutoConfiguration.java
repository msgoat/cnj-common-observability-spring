package group.msg.at.cloud.common.observability.logging.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for logging context information.
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
@ConditionalOnProperty(name = "cnj.observability.logging.mdc.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingMdcAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${cnj.observability.logging.mdc.enabled:true}")
    private boolean enabled;

    @Value("${cnj.observability.logging.mdc.urlPatterns:/api/*}")
    private String urlPatterns;

    @Bean
    public FilterRegistrationBean<LoggingMdcFilter> loggingMdcFilter() {
        logger.info("*** CONFIG *** Adding logging MDC filter to application context with enabled [{}]", enabled);
        FilterRegistrationBean<LoggingMdcFilter> result = new FilterRegistrationBean<>();
        LoggingMdcFilter filter = new LoggingMdcFilter();
        filter.setEnabled(this.enabled);
        result.setFilter(filter);
        result.addUrlPatterns(this.urlPatterns);
        result.setOrder(5000);
        return result;
    }
}
