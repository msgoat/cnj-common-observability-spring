package group.msg.at.cloud.common.observability.spring.rest.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * {@code RestTemplate} interceptor which logs outbound REST requests and inbound REST responses.
 */
public final class ClientRestTraceInterceptor implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(RestTraceConstants.REST_TRACE_LOGGER_NAME);

    private final RestTraceMessageBuilder messageBuilder = new DefaultRestTraceMessageBuilder();

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(request, response);
        return response;
    }

    private void traceRequest(HttpRequest request) {
        if (this.enabled) {
            StringBuilder traceMessage = new StringBuilder();
            this.messageBuilder.build(traceMessage, request);
            logger.info(traceMessage.toString());
        }
    }

    private void traceResponse(HttpRequest request, ClientHttpResponse response) {
        if (this.enabled) {
            StringBuilder traceMessage = new StringBuilder();
            this.messageBuilder.build(traceMessage, request, response);
            logger.info(traceMessage.toString());
        }
    }
}
