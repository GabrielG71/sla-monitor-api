package com.slamonitor.processor.domain.port;

import com.slamonitor.processor.domain.model.Check;

public interface CheckRepository {
    void save(Check check);
}
