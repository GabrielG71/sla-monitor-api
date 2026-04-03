package com.slamonitor.ingestor.domain.port;

import com.slamonitor.ingestor.domain.model.CheckResult;

public interface CheckResultPublisher {
    void publish(CheckResult result);
}
