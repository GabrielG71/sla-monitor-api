package com.slamonitor.processor.infrastructure.persistence;

import com.slamonitor.processor.domain.model.DeadLetter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface SpringDeadLetterJpaRepository extends JpaRepository<DeadLetter, UUID> {

    @Query("SELECT d FROM DeadLetter d ORDER BY d.failedAt DESC")
    List<DeadLetter> findRecent(Pageable pageable);
}
