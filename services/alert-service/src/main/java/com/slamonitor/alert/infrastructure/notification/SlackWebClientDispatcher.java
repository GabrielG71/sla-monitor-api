package com.slamonitor.alert.infrastructure.notification;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.port.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Dispatches alerts to a Slack channel via an Incoming Webhook URL.
 * Configure {@code notification.slack.webhook-url} to enable.
 */
@Component
@SuppressWarnings("null")
public class SlackWebClientDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(SlackWebClientDispatcher.class);

    private final WebClient webClient;

    @Value("${notification.slack.webhook-url:}")
    private String slackWebhookUrl;

    public SlackWebClientDispatcher(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public boolean isEnabled() {
        return !slackWebhookUrl.isBlank();
    }

    @Override
    public void dispatch(Alert alert) {
        String detail = alert.getMetadata() != null
                ? alert.getMetadata().getOrDefault("detail", "") : "";
        String ruleType = alert.getMetadata() != null
                ? alert.getMetadata().getOrDefault("ruleType", "UNKNOWN") : "UNKNOWN";

        String text = String.format("*[%s] %s violation*\nEndpoint: `%s`\n%s",
                alert.getSeverity().name(), ruleType,
                alert.getEndpointId(), detail);

        var body = Map.of(
                "text", text,
                "attachments", List.of(Map.of(
                        "color", severityColor(alert.getSeverity().name()),
                        "fields", List.of(
                                Map.of("title", "Alert ID",    "value", alert.getId().toString(),        "short", true),
                                Map.of("title", "Triggered At","value", alert.getTriggeredAt().toString(),"short", true)
                        )
                ))
        );

        webClient.post()
                .uri(slackWebhookUrl)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .subscribe(
                        r  -> log.info("Slack notification sent for alert {}", alert.getId()),
                        ex -> log.error("Slack dispatch failed for alert {}: {}",
                                alert.getId(), ex.getMessage())
                );
    }

    private static String severityColor(String severity) {
        return switch (severity) {
            case "CRITICAL" -> "#d9534f";
            case "WARNING"  -> "#f0ad4e";
            default         -> "#5bc0de";
        };
    }
}
