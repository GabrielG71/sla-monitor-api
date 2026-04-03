package com.slamonitor.ingestor.domain.port;

import com.slamonitor.ingestor.domain.model.CheckResult;
import com.slamonitor.ingestor.domain.model.Endpoint;

public interface HttpPoller {
    CheckResult poll(Endpoint endpoint);
}
