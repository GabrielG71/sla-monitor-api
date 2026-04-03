package com.slamonitor.ingestor.infrastructure.kafka;

import com.slamonitor.ingestor.domain.model.CheckResult;
import com.slamonitor.ingestor.domain.port.CheckResultPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("null")
public class CheckResultKafkaProducer implements CheckResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(CheckResultKafkaProducer.class);
    private static final String TOPIC = "raw-checks";

    private final KafkaTemplate<String, CheckResult> kafkaTemplate;

    public CheckResultKafkaProducer(KafkaTemplate<String, CheckResult> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(CheckResult result) {
        kafkaTemplate.send(TOPIC, result.endpointId().toString(), result)
                .whenComplete((r, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish check result for endpoint {}: {}",
                                result.endpointId(), ex.getMessage());
                    }
                });
    }
}
