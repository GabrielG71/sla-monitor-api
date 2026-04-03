package com.slamonitor.processor.application.usecase;

import com.slamonitor.processor.domain.model.Check;
import com.slamonitor.processor.domain.model.CheckResult;
import com.slamonitor.processor.domain.model.SlaOkResult;
import com.slamonitor.processor.domain.model.SlaRule;
import com.slamonitor.processor.domain.model.SlaViolation;
import com.slamonitor.processor.domain.port.CheckRepository;
import com.slamonitor.processor.domain.port.OkPublisher;
import com.slamonitor.processor.domain.port.SlaRuleRepository;
import com.slamonitor.processor.domain.port.ViolationPublisher;
import com.slamonitor.processor.domain.service.AvailabilityEvaluator;
import com.slamonitor.processor.domain.service.ErrorRateEvaluator;
import com.slamonitor.processor.domain.service.LatencyEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvaluateCheckUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluateCheckUseCase.class);

    private final CheckRepository checkRepository;
    private final SlaRuleRepository ruleRepository;
    private final AvailabilityEvaluator availabilityEvaluator;
    private final LatencyEvaluator latencyEvaluator;
    private final ErrorRateEvaluator errorRateEvaluator;
    private final ViolationPublisher violationPublisher;
    private final OkPublisher okPublisher;

    public EvaluateCheckUseCase(CheckRepository checkRepository,
                                SlaRuleRepository ruleRepository,
                                AvailabilityEvaluator availabilityEvaluator,
                                LatencyEvaluator latencyEvaluator,
                                ErrorRateEvaluator errorRateEvaluator,
                                ViolationPublisher violationPublisher,
                                OkPublisher okPublisher) {
        this.checkRepository = checkRepository;
        this.ruleRepository = ruleRepository;
        this.availabilityEvaluator = availabilityEvaluator;
        this.latencyEvaluator = latencyEvaluator;
        this.errorRateEvaluator = errorRateEvaluator;
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
            switch (rule.getRuleType()) {
                case AVAILABILITY -> evaluateAvailability(rule, checkResult);
                case LATENCY      -> evaluateLatency(rule, checkResult);
                case ERROR_RATE   -> evaluateErrorRate(rule, checkResult);
            }
        }
    }

    private void evaluateAvailability(SlaRule rule, CheckResult check) {
        int failures = availabilityEvaluator.evaluate(rule, check.success());
        if (failures > 0) {
            log.warn("AVAILABILITY violation on endpoint {}: {} consecutive failures",
                    check.endpointId(), failures);
            violationPublisher.publish(SlaViolation.forAvailability(rule, check, failures));
        } else {
            okPublisher.publish(new SlaOkResult(check.endpointId(), rule.getId(), check.checkedAt()));
        }
    }

    private void evaluateLatency(SlaRule rule, CheckResult check) {
        double p95 = latencyEvaluator.evaluate(rule, check);
        if (p95 > 0) {
            log.warn("LATENCY violation on endpoint {}: p95={}ms (threshold={}ms)",
                    check.endpointId(), (long) p95, rule.getThresholdValue().longValue());
            violationPublisher.publish(SlaViolation.forLatency(rule, check, p95));
        } else {
            okPublisher.publish(new SlaOkResult(check.endpointId(), rule.getId(), check.checkedAt()));
        }
    }

    private void evaluateErrorRate(SlaRule rule, CheckResult check) {
        double errorRate = errorRateEvaluator.evaluate(rule, check);
        if (errorRate > 0) {
            log.warn("ERROR_RATE violation on endpoint {}: {}% (threshold={}%)",
                    check.endpointId(), String.format("%.1f", errorRate),
                    rule.getThresholdValue().toPlainString());
            violationPublisher.publish(SlaViolation.forErrorRate(rule, check, errorRate));
        } else {
            okPublisher.publish(new SlaOkResult(check.endpointId(), rule.getId(), check.checkedAt()));
        }
    }
}
