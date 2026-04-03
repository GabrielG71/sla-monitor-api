package com.slamonitor.alert.infrastructure.persistence;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringAlertJpaRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByStatus(AlertStatus status);
}
