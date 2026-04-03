package com.slamonitor.processor.infrastructure.kafka;

import com.slamonitor.processor.application.usecase.EvaluateCheckUseCase;
import com.slamonitor.processor.domain.model.CheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RawCheckConsumer {

    private static final Logger log = LoggerFactory.getLogger(RawCheckConsumer.class);

    private final EvaluateCheckUseCase evaluateCheck;

    public RawCheckConsumer(EvaluateCheckUseCase evaluateCheck) {
        this.evaluateCheck = evaluateCheck;
    }

    @KafkaListener(topics = "raw-checks", groupId = "sla-processors")
    public void consume(CheckResult checkResult) {
        log.debug("Received check for endpoint {}: success={}",
                checkResult.endpointId(), checkResult.success());
        evaluateCheck.execute(checkResult);
    }
}
