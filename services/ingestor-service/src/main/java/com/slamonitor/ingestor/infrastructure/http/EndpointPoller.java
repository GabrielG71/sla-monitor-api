package com.slamonitor.ingestor.infrastructure.http;

import com.slamonitor.ingestor.domain.model.CheckResult;
import com.slamonitor.ingestor.domain.model.Endpoint;
import com.slamonitor.ingestor.domain.port.HttpPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;

@Component
@SuppressWarnings("null")
public class EndpointPoller implements HttpPoller {

    private static final Logger log = LoggerFactory.getLogger(EndpointPoller.class);

    private final WebClient webClient;

    public EndpointPoller(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public CheckResult poll(Endpoint endpoint) {
        long start = System.currentTimeMillis();
        try {
            var response = webClient
                    .method(org.springframework.http.HttpMethod.valueOf(endpoint.getHttpMethod()))
                    .uri(endpoint.getUrl())
                    .headers(h -> {
                        if (endpoint.getHeaders() != null) {
                            endpoint.getHeaders().forEach(h::set);
                        }
                    })
                    .exchangeToMono(r -> r.toBodilessEntity())
                    .timeout(Duration.ofMillis(endpoint.getTimeoutMs()))
                    .block();

            int latencyMs = (int) (System.currentTimeMillis() - start);
            int statusCode = response.getStatusCode().value();
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new CheckResult(endpoint.getId(), Instant.now(), statusCode, latencyMs, success, null);

        } catch (Exception ex) {
            int latencyMs = (int) (System.currentTimeMillis() - start);
            log.warn("Poll failed for endpoint {}: {}", endpoint.getId(), ex.getMessage());
            return new CheckResult(endpoint.getId(), Instant.now(), null, latencyMs, false,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }
}
