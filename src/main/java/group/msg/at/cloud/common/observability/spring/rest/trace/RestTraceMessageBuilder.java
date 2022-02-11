package group.msg.at.cloud.common.observability.spring.rest.trace;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * {@code Builder} for REST request and response trace messages.
 */
public interface RestTraceMessageBuilder {

    /**
     * Builds a trace message for the given outgoing request.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      outgoing request
     */
    void build(@NotNull StringBuilder traceMessage, @NotNull HttpRequest request);

    /**
     * Builds a trace message for the given incoming request.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      incoming request
     */
    void build(StringBuilder traceMessage, HttpServletRequest request);

    /**
     * Builds a trace message for the given incoming response.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      outgoing request
     * @param response     incoming response
     */
    void build(StringBuilder traceMessage, HttpRequest request, ClientHttpResponse response);

    /**
     * Builds a trace message for the given outgoing response.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      incoming request
     * @param response     outgoing response
     */
    void build(StringBuilder traceMessage, HttpServletRequest request, HttpServletResponse response);
}
