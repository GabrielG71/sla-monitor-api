package com.slamonitor.alert.infrastructure.notification;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.port.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Component
@SuppressWarnings("null")
public class WebhookWebClientDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebhookWebClientDispatcher.class);

    private final WebClient webClient;

    @Value("${notification.webhook.url:}")
    private String webhookUrl;

    public WebhookWebClientDispatcher(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public boolean isEnabled() {
        return !webhookUrl.isBlank();
    }

    @Override
    public void dispatch(Alert alert) {
        var payload = Map.of(
                "alertId",     alert.getId().toString(),
                "endpointId",  alert.getEndpointId().toString(),
                "ruleId",      alert.getSlaRuleId().toString(),
                "severity",    alert.getSeverity().name(),
                "triggeredAt", alert.getTriggeredAt().toString(),
                "detail",      alert.getMetadata() != null
                               ? alert.getMetadata().getOrDefault("detail", "") : ""
        );

        webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .subscribe(
                        r  -> log.info("Webhook dispatched for alert {}", alert.getId()),
                        ex -> log.error("Webhook dispatch failed for alert {}: {}",
                                alert.getId(), ex.getMessage())
                );
    }
}
