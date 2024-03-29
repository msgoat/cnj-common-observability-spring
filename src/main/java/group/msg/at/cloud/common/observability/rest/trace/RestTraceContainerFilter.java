package group.msg.at.cloud.common.observability.rest.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * {@code Servlet Filter} which traces incoming REST requests and outgoing REST responses.
 */
public final class RestTraceContainerFilter extends OncePerRequestFilter {

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
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !this.enabled || !this.logger.isInfoEnabled();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        traceRequest(request);
        filterChain.doFilter(request, response);
        traceResponse(request, response);
    }

    private void traceRequest(HttpServletRequest request) {
        StringBuilder traceMessage = new StringBuilder();
        this.messageBuilder.build(traceMessage, request);
        logger.info(traceMessage.toString());
    }

    private void traceResponse(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder traceMessage = new StringBuilder();
        this.messageBuilder.build(traceMessage, request, response);
        logger.info(traceMessage.toString());
    }
}
