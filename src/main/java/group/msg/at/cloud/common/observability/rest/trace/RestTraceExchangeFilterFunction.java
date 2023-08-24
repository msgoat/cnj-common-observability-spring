package group.msg.at.cloud.common.observability.rest.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * {@code ExchangeFilterFunction} which log outbound requests and inbound responses.
 */
public class RestTraceExchangeFilterFunction implements ExchangeFilterFunction {
    private final Logger logger = LoggerFactory.getLogger(RestTraceConstants.REST_TRACE_LOGGER_NAME);

    private final RestTraceMessageBuilder messageBuilder = new DefaultRestTraceMessageBuilder();

    private final boolean enabled;

    public RestTraceExchangeFilterFunction(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return shouldFilter(request) ? doFilter(request, next) : next.exchange(request);
    }

    private boolean shouldFilter(ClientRequest request) {
        return enabled && logger.isInfoEnabled();
    }

    private Mono<ClientResponse> doFilter(ClientRequest request, ExchangeFunction next) {
        traceRequest(request);
        Mono<ClientResponse> result = next.exchange(request);
        result = traceResponse(request, result);
        return result;
    }

    private void traceRequest(ClientRequest request) {
        StringBuilder traceMessage = new StringBuilder();
        this.messageBuilder.build(traceMessage, request);
        logger.info(traceMessage.toString());
    }

    private Mono<ClientResponse> traceResponse(ClientRequest request, Mono<ClientResponse> response) {
        return response.doOnSuccess(new SuccessResponseTracer(request, messageBuilder, logger));
    }

    private static final class SuccessResponseTracer implements Consumer<ClientResponse> {
        final Logger logger;
        final ClientRequest request;
        final RestTraceMessageBuilder messageBuilder;
        SuccessResponseTracer(ClientRequest request, RestTraceMessageBuilder messageBuilder, Logger logger) {
            this.request = request;
            this.messageBuilder = messageBuilder;
            this.logger = logger;
        }

        @Override
        public void accept(ClientResponse response) {
            StringBuilder traceMessage = new StringBuilder();
            this.messageBuilder.build(traceMessage, request, response);
            logger.info(traceMessage.toString());
        }
    }
}
