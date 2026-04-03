package com.slamonitor.processor.infrastructure.kafka;

import com.slamonitor.processor.domain.model.DeadLetter;
import com.slamonitor.processor.domain.port.DeadLetterRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Consumes messages from the Dead Letter Topic and persists them for inspection.
 * Uses a separate listener factory configured with StringDeserializer.
 */
@Component
public class DltConsumer {

    private static final Logger log = LoggerFactory.getLogger(DltConsumer.class);

    private final DeadLetterRepository deadLetterRepository;

    public DltConsumer(DeadLetterRepository deadLetterRepository) {
        this.deadLetterRepository = deadLetterRepository;
    }

    @KafkaListener(topics = "raw-checks.DLT", containerFactory = "dltListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        String errorClass = headerValue(record, "kafka_dlt-exception-fqcn");
        String errorMsg   = headerValue(record, "kafka_dlt-exception-message");

        log.warn("Dead letter received from partition={} offset={}: {}",
                record.partition(), record.offset(), errorClass);

        deadLetterRepository.save(new DeadLetter(
                record.topic(),
                record.partition(),
                record.offset(),
                Instant.now(),
                errorClass,
                errorMsg,
                record.value()
        ));
    }

    private static String headerValue(ConsumerRecord<?, ?> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
