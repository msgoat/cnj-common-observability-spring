package group.msg.at.cloud.common.observability.rest.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.*;

/**
 * Default implementation of a {@code RestTraceMessageBuilder}.
 */
public class DefaultRestTraceMessageBuilder implements RestTraceMessageBuilder {

    private static final Set<String> CONFIDENTIAL_HEADER_NAMES = Set.of("Authorization");

    @Override
    public void build(StringBuilder traceMessage, HttpRequest request) {
        traceMessage.append("*** REST REQUEST OUT *** { ");
        appendRequest(traceMessage, request, true);
        traceMessage.append(" }");
    }

    @Override
    public void build(StringBuilder traceMessage, HttpServletRequest request) {
        traceMessage.append("*** REST REQUEST IN *** { ");
        appendRequest(traceMessage, request, true);
        traceMessage.append(" }");
    }

    @Override
    public void build(StringBuilder traceMessage, HttpRequest request, ClientHttpResponse response) {
        traceMessage.append("*** REST RESPONSE IN *** { ");
        appendRequest(traceMessage, request, false);
        traceMessage.append(", ");
        appendResponse(traceMessage, response);
        traceMessage.append(" }");
    }

    @Override
    public void build(StringBuilder traceMessage, HttpServletRequest request, HttpServletResponse response) {
        traceMessage.append("*** REST RESPONSE OUT *** { ");
        appendRequest(traceMessage, request, false);
        traceMessage.append(", ");
        appendResponse(traceMessage, response);
        traceMessage.append(" }");
    }

    private void appendRequest(StringBuilder traceMessage, HttpRequest request, boolean withHeaders) {
        traceMessage.append("request : { ");
        traceMessage.append("uri : \"").append(request.getURI()).append("\"");
        traceMessage.append(", method : \"").append(request.getMethod()).append("\"");
        if (withHeaders) {
            traceMessage.append(", ");
            appendHeaders(traceMessage, request.getHeaders());
        }
        traceMessage.append(" }");
    }

    private void appendRequest(StringBuilder traceMessage, HttpServletRequest request, boolean withHeaders) {
        traceMessage.append("request : { ");
        traceMessage.append("uri : \"").append(request.getRequestURL()).append("\"");
        traceMessage.append(", method : \"").append(request.getMethod()).append("\"");
        if (withHeaders) {
            traceMessage.append(", ");
            appendHeaders(traceMessage, request);
        }
        traceMessage.append(" }");
    }

    private void appendResponse(StringBuilder traceMessage, ClientHttpResponse response) {
        traceMessage.append("response { ");
        int statusCode = extractStatusCode(response);
        traceMessage.append("statusCode : ").append(statusCode);
        String statusText = extractStatusMessage(response);
        if (statusText != null && !statusText.isEmpty()) {
            traceMessage.append(", statusText : \"").append(statusText).append("\"");
        }
        traceMessage.append(", ");
        appendHeaders(traceMessage, response.getHeaders());
        traceMessage.append(" }");
    }

    private void appendResponse(StringBuilder traceMessage, HttpServletResponse response) {
        traceMessage.append("response { ");
        traceMessage.append("statusCode : ").append(response.getStatus());
        traceMessage.append(", ");
        appendHeaders(traceMessage, response);
        traceMessage.append(" }");
    }

    private void appendHeaders(StringBuilder traceEntry, HttpHeaders headers) {
        traceEntry.append("headers : { ");
        int headerIndex = 0;
        for (Map.Entry<String, List<String>> currentHeader : headers.entrySet()) {
            if (headerIndex > 0) {
                traceEntry.append(", ");
            }
            traceEntry.append(currentHeader.getKey()).append(" : ");
            if (currentHeader.getValue().size() == 1) {
                traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeader.getKey(), currentHeader.getValue().get(0))).append("\"");
            } else {
                traceEntry.append("[");
                int valueIndex = 0;
                for (String currentHeaderValue : currentHeader.getValue()) {
                    if (valueIndex > 0) {
                        traceEntry.append(", ");
                    }
                    traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeader.getKey(), currentHeaderValue)).append("\"");
                    valueIndex++;
                }
                traceEntry.append("]");
            }
            headerIndex++;
        }
        traceEntry.append(" }");
    }

    private void appendHeaders(StringBuilder traceEntry, HttpServletRequest request) {
        traceEntry.append("headers : { ");
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            int headerIndex = 0;
            while (headerNames.hasMoreElements()) {
                String currentHeaderName = headerNames.nextElement();
                if (headerIndex > 0) {
                    traceEntry.append(", ");
                }
                traceEntry.append(currentHeaderName).append(" : ");
                traceEntry.append("[");
                Enumeration<String> headerValues = request.getHeaders(currentHeaderName);
                if (headerValues != null) {
                    int valueIndex = 0;
                    while (headerValues.hasMoreElements()) {
                        if (valueIndex > 0) {
                            traceEntry.append(", ");
                        }
                        traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeaderName, headerValues.nextElement())).append("\"");
                        valueIndex++;
                    }
                }
                traceEntry.append("]");
                headerIndex++;
            }
        }
        traceEntry.append(" }");
    }

    private void appendHeaders(StringBuilder traceEntry, HttpServletResponse response) {
        traceEntry.append("headers : { ");
        int headerIndex = 0;
        for (String currentHeaderName : response.getHeaderNames()) {
            if (headerIndex > 0) {
                traceEntry.append(", ");
            }
            traceEntry.append(currentHeaderName).append(" : ");
            Collection<String> headerValues = response.getHeaders(currentHeaderName);
            if (headerValues.size() == 1) {
                traceEntry.append("\"").append(headerValues.iterator().next()).append("\"");
            } else {
                traceEntry.append("[");
                int valueIndex = 0;
                for (String currentHeaderValue : headerValues) {
                    if (valueIndex > 0) {
                        traceEntry.append(", ");
                    }
                    traceEntry.append("\"").append(currentHeaderValue).append("\"");
                    valueIndex++;
                }
                traceEntry.append("]");
            }
            headerIndex++;
        }
        traceEntry.append(" }");
    }

    private int extractStatusCode(ClientHttpResponse response) {
        int result = -1;
        try {
            result = response.getRawStatusCode();
        } catch (IOException ex) {
            // we don't care !!!
        }
        return result;
    }

    private String extractStatusMessage(ClientHttpResponse response) {
        String result = null;
        try {
            result = response.getStatusText();
        } catch (IOException ex) {
            // we don't care !!!
        }
        return result;
    }

    private String filterConfidentialHeaderValue(String headerName, String headerValue) {
        return CONFIDENTIAL_HEADER_NAMES.contains(headerName) ? String.format("_redacted(%d)_", headerValue.length()) : headerValue;
    }
}
