package com.slamonitor.processor.domain.port;

import com.slamonitor.processor.domain.model.DeadLetter;

import java.util.List;

public interface DeadLetterRepository {
    void save(DeadLetter deadLetter);
    List<DeadLetter> findRecent(int limit);
}
