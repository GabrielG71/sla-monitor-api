package com.slamonitor.alert.infrastructure.kafka;

import com.slamonitor.alert.application.usecase.DispatchAlertUseCase;
import com.slamonitor.alert.domain.model.SlaViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SlaViolationConsumer {

    private static final Logger log = LoggerFactory.getLogger(SlaViolationConsumer.class);

    private final DispatchAlertUseCase dispatchAlert;

    public SlaViolationConsumer(DispatchAlertUseCase dispatchAlert) {
        this.dispatchAlert = dispatchAlert;
    }

    @KafkaListener(topics = "sla-violations", groupId = "alert-dispatchers")
    public void consume(SlaViolation violation) {
        log.info("Violation received — endpoint={} rule={} severity={}",
                violation.endpointId(), violation.ruleId(), violation.severity());
        dispatchAlert.execute(violation);
    }
}
