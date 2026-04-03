package com.slamonitor.processor.infrastructure.persistence;

import com.slamonitor.processor.domain.model.Check;
import com.slamonitor.processor.domain.port.CheckRepository;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("null")
public class CheckJpaAdapter implements CheckRepository {

    private final SpringCheckJpaRepository jpa;

    public CheckJpaAdapter(SpringCheckJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Check check) {
        jpa.save(check);
    }
}
