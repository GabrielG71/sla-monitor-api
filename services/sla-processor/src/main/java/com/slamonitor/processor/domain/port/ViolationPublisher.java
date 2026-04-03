package com.slamonitor.processor.domain.port;

import com.slamonitor.processor.domain.model.SlaViolation;

public interface ViolationPublisher {
    void publish(SlaViolation violation);
}
