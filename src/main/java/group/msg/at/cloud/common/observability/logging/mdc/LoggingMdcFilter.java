package group.msg.at.cloud.common.observability.logging.mdc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * {@code Servlet Filter} which adds context information to the underlying logging framework.
 * <p>
 * Adds the following context information to the logging context of the current thread (if available):
 * <ul>
 * <li>Trace-ID of the current {@code OpenTracing} or {@code OpenTelemetry} trace</li>
 * <li>Principal name of the currently authenticated user</li>
 * </ul>
 * </p>
 */
public final class LoggingMdcFilter extends OncePerRequestFilter {

    private static final String MDC_PROPERTY_NAME_TRACE_ID = "traceId";
    private static final String MDC_PROPERTY_NAME_USER_ID = "userId";

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !isEnabled() || request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        addContextInfo(request);
        try {
            filterChain.doFilter(request, response);
        } finally {
            clearContext(request, response);
        }
    }

    private void addContextInfo(HttpServletRequest request) {
        if (MDC.get(MDC_PROPERTY_NAME_TRACE_ID) == null) {
            String jaegerTraceHeader = request.getHeader("uber-trace-id");
            if (jaegerTraceHeader != null) {
                String[] traceIdComponents = jaegerTraceHeader.split(":");
                if (traceIdComponents.length == 1) {
                    // OpenTracing header exists but is URL encoded
                    traceIdComponents = jaegerTraceHeader.split("%3A");
                }
                MDC.put("traceId", traceIdComponents[0]);
            }
            String w3cTraceHeader = request.getHeader("traceparent");
            if (w3cTraceHeader != null) {
                String traceId = w3cTraceHeader.split("-")[1];
                MDC.put(MDC_PROPERTY_NAME_TRACE_ID, traceId);
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            MDC.put(MDC_PROPERTY_NAME_USER_ID, auth.getName());
        }
    }

    private void clearContext(HttpServletRequest request, HttpServletResponse response) {
        MDC.clear();
    }
}
