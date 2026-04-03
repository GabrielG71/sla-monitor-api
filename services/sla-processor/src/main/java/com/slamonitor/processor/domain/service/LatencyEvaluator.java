package com.slamonitor.processor.domain.service;

import com.slamonitor.processor.domain.model.CheckResult;
import com.slamonitor.processor.domain.model.SlaRule;
import com.slamonitor.processor.domain.port.LatencyWindowRepository;
import org.springframework.stereotype.Service;

@Service
public class LatencyEvaluator {

    private final LatencyWindowRepository windowRepository;

    public LatencyEvaluator(LatencyWindowRepository windowRepository) {
        this.windowRepository = windowRepository;
    }

    /**
     * Records the latency for this check and evaluates the p95 against the rule threshold.
     *
     * @return the current p95 latency (ms) if it exceeds the threshold, or 0 if no violation.
     */
    public double evaluate(SlaRule rule, CheckResult check) {
        if (check.latencyMs() == null) return 0;

        windowRepository.record(check.endpointId(), check.latencyMs(), rule.getWindowSeconds());
        double p95 = windowRepository.getPercentile(check.endpointId(), 95.0);
        double threshold = rule.getThresholdValue().doubleValue();

        return p95 > threshold ? p95 : 0;
    }
}
