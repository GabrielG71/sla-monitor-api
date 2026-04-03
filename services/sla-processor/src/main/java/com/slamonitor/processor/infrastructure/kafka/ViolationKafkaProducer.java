package com.slamonitor.processor.infrastructure.kafka;

import com.slamonitor.processor.domain.model.SlaViolation;
import com.slamonitor.processor.domain.port.ViolationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("null")
public class ViolationKafkaProducer implements ViolationPublisher {

    private static final Logger log = LoggerFactory.getLogger(ViolationKafkaProducer.class);
    private static final String TOPIC = "sla-violations";

    private final KafkaTemplate<String, SlaViolation> kafkaTemplate;

    public ViolationKafkaProducer(KafkaTemplate<String, SlaViolation> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(SlaViolation violation) {
        kafkaTemplate.send(TOPIC, violation.endpointId().toString(), violation)
                .whenComplete((r, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish violation for endpoint {}: {}",
                                violation.endpointId(), ex.getMessage());
                    }
                });
    }
}
