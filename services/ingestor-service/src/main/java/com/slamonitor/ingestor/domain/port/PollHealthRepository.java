package com.slamonitor.ingestor.domain.port;

import com.slamonitor.ingestor.domain.model.CheckResult;
import com.slamonitor.ingestor.domain.model.PollHealth;

import java.util.Optional;
import java.util.UUID;

public interface PollHealthRepository {
    void record(CheckResult result, int ttlSeconds);
    Optional<PollHealth> getLatest(UUID endpointId);
}
