package com.slamonitor.alert.application.usecase;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.model.SlaViolation;
import com.slamonitor.alert.domain.port.AlertRepository;
import com.slamonitor.alert.domain.port.AlertStreamPublisher;
import com.slamonitor.alert.domain.port.AlertThrottleRepository;
import com.slamonitor.alert.domain.port.WebhookDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DispatchAlertUseCase {

    private static final Logger log = LoggerFactory.getLogger(DispatchAlertUseCase.class);

    private final AlertRepository alertRepository;
    private final AlertThrottleRepository throttleRepository;
    private final WebhookDispatcher webhookDispatcher;
    private final AlertStreamPublisher streamPublisher;

    @Value("${alert.throttle.default-window-secs:300}")
    private int throttleWindowSecs;

    public DispatchAlertUseCase(AlertRepository alertRepository,
                                AlertThrottleRepository throttleRepository,
                                WebhookDispatcher webhookDispatcher,
                                AlertStreamPublisher streamPublisher) {
        this.alertRepository = alertRepository;
        this.throttleRepository = throttleRepository;
        this.webhookDispatcher = webhookDispatcher;
        this.streamPublisher = streamPublisher;
    }

    @Transactional
    public Alert execute(SlaViolation violation) {
        var alert = alertRepository.save(Alert.open(violation));

        if (throttleRepository.tryAcquire(violation.ruleId(), throttleWindowSecs)) {
            webhookDispatcher.dispatch(alert);
        } else {
            log.debug("Alert for rule {} throttled — suppressing webhook dispatch", violation.ruleId());
        }

        streamPublisher.publish(alert);
        return alert;
    }
}
