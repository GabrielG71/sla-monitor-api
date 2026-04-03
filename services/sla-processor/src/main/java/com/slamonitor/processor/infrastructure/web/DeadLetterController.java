package com.slamonitor.processor.infrastructure.web;

import com.slamonitor.processor.domain.model.DeadLetter;
import com.slamonitor.processor.domain.port.DeadLetterRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dead-letters")
public class DeadLetterController {

    private final DeadLetterRepository deadLetterRepository;

    public DeadLetterController(DeadLetterRepository deadLetterRepository) {
        this.deadLetterRepository = deadLetterRepository;
    }

    @GetMapping
    public List<DeadLetterResponse> list(@RequestParam(defaultValue = "100") int limit) {
        return deadLetterRepository.findRecent(Math.min(limit, 500)).stream()
                .map(DeadLetterResponse::from)
                .toList();
    }

    public record DeadLetterResponse(
            UUID id, String topic, int partitionN, long offsetN,
            Instant failedAt, String errorClass, String errorMsg, String payload) {

        static DeadLetterResponse from(DeadLetter d) {
            return new DeadLetterResponse(d.getId(), d.getTopic(), d.getPartitionN(),
                    d.getOffsetN(), d.getFailedAt(), d.getErrorClass(), d.getErrorMsg(),
                    d.getPayload());
        }
    }
}
