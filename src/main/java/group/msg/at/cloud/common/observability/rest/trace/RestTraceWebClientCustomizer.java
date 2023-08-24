package group.msg.at.cloud.common.observability.rest.trace;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@code WebClientCustomizer} that adds the {@code RestTraceExchangeFilterFunction}
 * to the exchange filter function chain of a given {@code WebClient.Builder} in
 * order to ensure that outbound requests and inbound responses are traced properly.
 *
 * @author Michael Theis (msg)
 */
public class RestTraceWebClientCustomizer implements WebClientCustomizer {

    private boolean enabled;

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        webClientBuilder.filter(new RestTraceExchangeFilterFunction(enabled));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
