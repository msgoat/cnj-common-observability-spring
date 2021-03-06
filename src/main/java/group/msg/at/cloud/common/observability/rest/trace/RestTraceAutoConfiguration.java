package group.msg.at.cloud.common.observability.rest.trace;

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

    @Value("${cnj.observability.rest.tracing.enabled:false}")
    private boolean enabled;

    @Value("${cnj.observability.rest.tracing.urlPatterns:/api/*}")
    private String urlPatterns;

    @Bean
    public FilterRegistrationBean<ContainerRestTraceFilter> containerRestTraceFilter() {
        FilterRegistrationBean<ContainerRestTraceFilter> result = new FilterRegistrationBean<>();
        ContainerRestTraceFilter filter = new ContainerRestTraceFilter();
        filter.setEnabled(this.enabled);
        result.setFilter(filter);
        result.addUrlPatterns(this.urlPatterns);
        return result;
    }

    @Bean
    public ClientRestTraceInterceptor clientRestTraceInterceptor() {
        ClientRestTraceInterceptor result = new ClientRestTraceInterceptor();
        result.setEnabled(this.enabled);
        return result;
    }

    @Bean
    public RestTraceRestTemplateCustomizer clientRestTraceInterceptorCustomizer() {
        return new RestTraceRestTemplateCustomizer(clientRestTraceInterceptor());
    }
}
