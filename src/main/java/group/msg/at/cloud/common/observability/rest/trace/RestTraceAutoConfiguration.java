package group.msg.at.cloud.common.observability.rest.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for REST traces logged to application logs.
 * <p>
 * In order to have trace entries written to the application logs,
 * application property {@code cnj.observability.rest.tracing.enabled} must be set to true.
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "cnj.observability.rest.tracing.enabled", havingValue = "true")
public class RestTraceAutoConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${cnj.observability.rest.tracing.enabled:false}")
    private boolean enabled;

    @Value("${cnj.observability.rest.tracing.urlPatterns:/api/*}")
    private String urlPatterns;

    @Bean
    public FilterRegistrationBean<RestTraceContainerFilter> containerRestTraceFilter() {
        log.info("*** CONFIG *** Adding container rest trace filter to application context");
        FilterRegistrationBean<RestTraceContainerFilter> result = new FilterRegistrationBean<>();
        RestTraceContainerFilter filter = new RestTraceContainerFilter();
        filter.setEnabled(this.enabled);
        result.setFilter(filter);
        result.addUrlPatterns(this.urlPatterns);
        return result;
    }

    @Bean
    public RestTraceClientInterceptor clientRestTraceInterceptor() {
        log.info("*** CONFIG *** Adding client rest trace interceptor to application context");
        RestTraceClientInterceptor result = new RestTraceClientInterceptor();
        result.setEnabled(this.enabled);
        return result;
    }

    @Bean
    public RestTraceRestTemplateCustomizer clientRestTraceInterceptorCustomizer() {
        log.info("*** CONFIG *** Adding client rest trace interceptor customizer to application context");
        return new RestTraceRestTemplateCustomizer(clientRestTraceInterceptor());
    }

    @Bean
    public RestTraceWebClientCustomizer restTraceWebClientCustomizer() {
        log.info("*** CONFIG *** Adding web client rest trace customizer to application context");
        RestTraceWebClientCustomizer result = new RestTraceWebClientCustomizer();
        result.setEnabled(this.enabled);
        return result;
    }
}
