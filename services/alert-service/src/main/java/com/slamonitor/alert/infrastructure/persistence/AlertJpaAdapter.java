package com.slamonitor.alert.infrastructure.persistence;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.model.AlertStatus;
import com.slamonitor.alert.domain.port.AlertRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@SuppressWarnings("null")
public class AlertJpaAdapter implements AlertRepository {

    private final SpringAlertJpaRepository jpa;

    public AlertJpaAdapter(SpringAlertJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Alert save(Alert alert) {
        return jpa.save(alert);
    }

    @Override
    public Optional<Alert> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Alert> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<Alert> findByStatus(AlertStatus status) {
        return jpa.findByStatus(status);
    }

    @Override
    public List<Alert> findByTriggeredAtBetween(Instant from, Instant to) {
        return jpa.findByTriggeredAtBetween(from, to);
    }

    @Override
    public List<Alert> findByEndpointIdAndTriggeredAtBetween(UUID endpointId, Instant from, Instant to) {
        return jpa.findByEndpointIdAndTriggeredAtBetween(endpointId, from, to);
    }
}
