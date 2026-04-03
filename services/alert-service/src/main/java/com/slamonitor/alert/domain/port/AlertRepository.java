package com.slamonitor.alert.domain.port;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.model.AlertStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository {
    Alert save(Alert alert);
    Optional<Alert> findById(UUID id);
    List<Alert> findAll();
    List<Alert> findByStatus(AlertStatus status);
    List<Alert> findByTriggeredAtBetween(Instant from, Instant to);
    List<Alert> findByEndpointIdAndTriggeredAtBetween(UUID endpointId, Instant from, Instant to);
}
