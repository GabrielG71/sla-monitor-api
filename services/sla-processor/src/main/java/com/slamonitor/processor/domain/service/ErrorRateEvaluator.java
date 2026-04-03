package com.slamonitor.processor.domain.service;

import com.slamonitor.processor.domain.model.CheckResult;
import com.slamonitor.processor.domain.model.SlaRule;
import com.slamonitor.processor.domain.port.ErrorRateWindowRepository;
import org.springframework.stereotype.Service;

@Service
public class ErrorRateEvaluator {

    private final ErrorRateWindowRepository windowRepository;

    public ErrorRateEvaluator(ErrorRateWindowRepository windowRepository) {
        this.windowRepository = windowRepository;
    }

    /**
     * Records the check outcome and evaluates the rolling error rate against the rule threshold.
     *
     * @return the current error rate (0–100) if it exceeds the threshold, or 0 if no violation.
     */
    public double evaluate(SlaRule rule, CheckResult check) {
        windowRepository.record(check.endpointId(), check.success(), rule.getWindowSeconds());
        double errorRate = windowRepository.getErrorRate(check.endpointId());
        double threshold = rule.getThresholdValue().doubleValue();

        return errorRate > threshold ? errorRate : 0;
    }
}
