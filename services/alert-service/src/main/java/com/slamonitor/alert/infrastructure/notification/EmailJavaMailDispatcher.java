package com.slamonitor.alert.infrastructure.notification;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.port.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Dispatches alerts via SMTP email.
 * Configure {@code notification.email.to} and {@code spring.mail.host} to enable.
 */
@Component
public class EmailJavaMailDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EmailJavaMailDispatcher.class);

    private final JavaMailSender mailSender;

    @Value("${notification.email.to:}")
    private String emailTo;

    @Value("${notification.email.from:sla-monitor@localhost}")
    private String emailFrom;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    public EmailJavaMailDispatcher(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean isEnabled() {
        return !smtpHost.isBlank() && !emailTo.isBlank();
    }

    @Override
    public void dispatch(Alert alert) {
        String ruleType = alert.getMetadata() != null
                ? alert.getMetadata().getOrDefault("ruleType", "UNKNOWN") : "UNKNOWN";
        String detail = alert.getMetadata() != null
                ? alert.getMetadata().getOrDefault("detail", "") : "";

        var message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(emailTo);
        message.setSubject(String.format("[SLA Monitor] %s %s violation on endpoint %s",
                alert.getSeverity().name(), ruleType, alert.getEndpointId()));
        message.setText(String.format(
                "Alert ID: %s%nEndpoint: %s%nRule: %s%nSeverity: %s%nTriggered: %s%nDetail: %s",
                alert.getId(), alert.getEndpointId(), ruleType,
                alert.getSeverity().name(), alert.getTriggeredAt(), detail));

        try {
            mailSender.send(message);
            log.info("Email notification sent for alert {}", alert.getId());
        } catch (Exception ex) {
            log.error("Email dispatch failed for alert {}: {}", alert.getId(), ex.getMessage());
        }
    }
}
