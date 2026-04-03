package com.slamonitor.alert.domain.port;

import com.slamonitor.alert.domain.model.CheckStats;

import java.time.Instant;
import java.util.List;

public interface CheckStatsRepository {
    List<CheckStats> summarize(Instant from, Instant to);
}
