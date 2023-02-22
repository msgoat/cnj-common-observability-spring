package group.msg.at.cloud.common.observability.logging.mdc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for logging context information.
 */
@Configuration
@ConditionalOnClass(name = "org/springframework/security/core/context/SecurityContextHolder")
public class LoggingMdcAutoConfiguration {

    @Value("${cnj.observability.logging.mdc.enabled:true}")
    private boolean enabled;

    @Value("${cnj.observability.logging.mdc.urlPatterns:/api/*}")
    private String urlPatterns;

    @Bean
    public FilterRegistrationBean<LoggingMdcFilter> loggingMdcFilter() {
        FilterRegistrationBean<LoggingMdcFilter> result = new FilterRegistrationBean<>();
        LoggingMdcFilter filter = new LoggingMdcFilter();
        filter.setEnabled(this.enabled);
        result.setFilter(filter);
        result.addUrlPatterns(this.urlPatterns);
        result.setOrder(5000);
        return result;
    }
}
