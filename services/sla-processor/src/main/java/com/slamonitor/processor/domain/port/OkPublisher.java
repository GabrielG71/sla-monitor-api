package com.slamonitor.processor.domain.port;

import com.slamonitor.processor.domain.model.SlaOkResult;

public interface OkPublisher {
    void publish(SlaOkResult result);
}
