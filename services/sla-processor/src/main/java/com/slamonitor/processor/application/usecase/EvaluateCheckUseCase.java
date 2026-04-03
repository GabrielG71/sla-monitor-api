package com.slamonitor.processor.application.usecase;

import com.slamonitor.processor.domain.model.Check;
import com.slamonitor.processor.domain.model.CheckResult;
import com.slamonitor.processor.domain.model.SlaOkResult;
import com.slamonitor.processor.domain.model.SlaViolation;
import com.slamonitor.processor.domain.model.SlaRuleType;
import com.slamonitor.processor.domain.port.CheckRepository;
import com.slamonitor.processor.domain.port.OkPublisher;
import com.slamonitor.processor.domain.port.SlaRuleRepository;
import com.slamonitor.processor.domain.port.ViolationPublisher;
import com.slamonitor.processor.domain.service.AvailabilityEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvaluateCheckUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluateCheckUseCase.class);

    private final CheckRepository checkRepository;
    private final SlaRuleRepository ruleRepository;
    private final AvailabilityEvaluator availabilityEvaluator;
    private final ViolationPublisher violationPublisher;
    private final OkPublisher okPublisher;

    public EvaluateCheckUseCase(CheckRepository checkRepository,
                                SlaRuleRepository ruleRepository,
                                AvailabilityEvaluator availabilityEvaluator,
                                ViolationPublisher violationPublisher,
                                OkPublisher okPublisher) {
        this.checkRepository = checkRepository;
        this.ruleRepository = ruleRepository;
        this.availabilityEvaluator = availabilityEvaluator;
        this.violationPublisher = violationPublisher;
        this.okPublisher = okPublisher;
    }

    public void execute(CheckResult checkResult) {
        checkRepository.save(Check.from(checkResult));

        var rules = ruleRepository.findActiveByEndpointId(checkResult.endpointId());
        if (rules.isEmpty()) {
            log.debug("No active SLA rules for endpoint {}", checkResult.endpointId());
            return;
        }

        for (var rule : rules) {
            if (rule.getRuleType() != SlaRuleType.AVAILABILITY) {
                continue; // LATENCY and ERROR_RATE handled in v2
            }

            int failures = availabilityEvaluator.evaluate(rule, checkResult.success());
            if (failures > 0) {
                log.warn("AVAILABILITY violation on endpoint {}: {} consecutive failures",
                        checkResult.endpointId(), failures);
                violationPublisher.publish(SlaViolation.from(rule, checkResult, failures));
            } else {
                okPublisher.publish(new SlaOkResult(
                        checkResult.endpointId(), rule.getId(), checkResult.checkedAt()));
            }
        }
    }
}
