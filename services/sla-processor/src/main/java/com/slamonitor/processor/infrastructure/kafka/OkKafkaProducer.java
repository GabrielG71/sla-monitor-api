package com.slamonitor.processor.infrastructure.kafka;

import com.slamonitor.processor.domain.model.SlaOkResult;
import com.slamonitor.processor.domain.port.OkPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("null")
public class OkKafkaProducer implements OkPublisher {

    private static final String TOPIC = "sla-ok";

    private final KafkaTemplate<String, SlaOkResult> kafkaTemplate;

    public OkKafkaProducer(KafkaTemplate<String, SlaOkResult> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(SlaOkResult result) {
        kafkaTemplate.send(TOPIC, result.endpointId().toString(), result);
    }
}
