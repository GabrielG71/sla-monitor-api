package com.slamonitor.processor.infrastructure.persistence;

import com.slamonitor.processor.domain.model.DeadLetter;
import com.slamonitor.processor.domain.port.DeadLetterRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("null")
public class DeadLetterJpaAdapter implements DeadLetterRepository {

    private final SpringDeadLetterJpaRepository jpa;

    public DeadLetterJpaAdapter(SpringDeadLetterJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(DeadLetter deadLetter) {
        jpa.save(deadLetter);
    }

    @Override
    public List<DeadLetter> findRecent(int limit) {
        return jpa.findRecent(PageRequest.of(0, limit));
    }
}
