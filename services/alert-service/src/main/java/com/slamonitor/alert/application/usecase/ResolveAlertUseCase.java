package com.slamonitor.alert.application.usecase;

import com.slamonitor.alert.domain.exception.AlertNotFoundException;
import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.port.AlertRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ResolveAlertUseCase {

    private final AlertRepository alertRepository;

    public ResolveAlertUseCase(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional
    public Alert execute(UUID id) {
        var alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException(id));
        alert.resolve();
        return alertRepository.save(alert);
    }
}
