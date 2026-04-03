package com.slamonitor.alert.application.usecase;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.model.SlaViolation;
import com.slamonitor.alert.domain.port.AlertRepository;
import com.slamonitor.alert.domain.port.AlertStreamPublisher;
import com.slamonitor.alert.domain.port.AlertThrottleRepository;
import com.slamonitor.alert.domain.port.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DispatchAlertUseCase {

    private static final Logger log = LoggerFactory.getLogger(DispatchAlertUseCase.class);

    private final AlertRepository alertRepository;
    private final AlertThrottleRepository throttleRepository;
    private final AlertStreamPublisher streamPublisher;
    private final List<NotificationDispatcher> dispatchers;

    @Value("${alert.throttle.default-window-secs:300}")
    private int defaultThrottleWindowSecs;

    public DispatchAlertUseCase(AlertRepository alertRepository,
                                AlertThrottleRepository throttleRepository,
                                AlertStreamPublisher streamPublisher,
                                List<NotificationDispatcher> dispatchers) {
        this.alertRepository = alertRepository;
        this.throttleRepository = throttleRepository;
        this.streamPublisher = streamPublisher;
        this.dispatchers = dispatchers;
    }

    @Transactional
    public Alert execute(SlaViolation violation) {
        var alert = alertRepository.save(Alert.open(violation));

        int throttleWindow = violation.windowSeconds() > 0
                ? violation.windowSeconds()
                : defaultThrottleWindowSecs;

        if (throttleRepository.tryAcquire(violation.ruleId(), throttleWindow)) {
            dispatchers.stream()
                    .filter(NotificationDispatcher::isEnabled)
                    .forEach(d -> d.dispatch(alert));
        } else {
            log.debug("Alert for rule {} throttled — suppressing notifications (window={}s)",
                    violation.ruleId(), throttleWindow);
        }

        streamPublisher.publish(alert);
        return alert;
    }
}
