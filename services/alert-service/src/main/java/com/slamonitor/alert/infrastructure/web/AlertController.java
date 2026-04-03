package com.slamonitor.alert.infrastructure.web;

import com.slamonitor.alert.application.usecase.AcknowledgeAlertUseCase;
import com.slamonitor.alert.application.usecase.ResolveAlertUseCase;
import com.slamonitor.alert.domain.exception.AlertNotFoundException;
import com.slamonitor.alert.domain.model.AlertStatus;
import com.slamonitor.alert.domain.port.AlertRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepository;
    private final AcknowledgeAlertUseCase acknowledgeAlert;
    private final ResolveAlertUseCase resolveAlert;

    public AlertController(AlertRepository alertRepository,
                           AcknowledgeAlertUseCase acknowledgeAlert,
                           ResolveAlertUseCase resolveAlert) {
        this.alertRepository = alertRepository;
        this.acknowledgeAlert = acknowledgeAlert;
        this.resolveAlert = resolveAlert;
    }

    @GetMapping
    public List<AlertResponse> list(@RequestParam(required = false) AlertStatus status) {
        var alerts = status != null
                ? alertRepository.findByStatus(status)
                : alertRepository.findAll();
        return alerts.stream().map(AlertResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AlertResponse getById(@PathVariable UUID id) {
        return alertRepository.findById(id)
                .map(AlertResponse::from)
                .orElseThrow(() -> new AlertNotFoundException(id));
    }

    @PatchMapping("/{id}/acknowledge")
    public AlertResponse acknowledge(@PathVariable UUID id) {
        return AlertResponse.from(acknowledgeAlert.execute(id));
    }

    @PatchMapping("/{id}/resolve")
    public AlertResponse resolve(@PathVariable UUID id) {
        return AlertResponse.from(resolveAlert.execute(id));
    }
}
