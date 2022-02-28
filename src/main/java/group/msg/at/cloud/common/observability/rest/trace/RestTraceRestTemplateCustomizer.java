package group.msg.at.cloud.common.observability.rest.trace;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatically adds a {@code ClientHttpRequestInterceptor} to any given {@code RestTemplate}.
 */
public final class RestTraceRestTemplateCustomizer implements RestTemplateCustomizer {

    private final ClientRestTraceInterceptor interceptor;

    RestTraceRestTemplateCustomizer(ClientRestTraceInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> customizedInterceptors = new ArrayList<>(restTemplate.getInterceptors());
        customizedInterceptors.add(interceptor);
        restTemplate.setInterceptors(customizedInterceptors);
    }
}
