package com.slamonitor.alert.infrastructure.web;

import com.slamonitor.alert.application.usecase.AcknowledgeAlertUseCase;
import com.slamonitor.alert.application.usecase.ResolveAlertUseCase;
import com.slamonitor.alert.domain.exception.AlertNotFoundException;
import com.slamonitor.alert.domain.model.AlertStatus;
import com.slamonitor.alert.domain.port.AlertRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepository;
    private final AcknowledgeAlertUseCase acknowledgeAlert;
    private final ResolveAlertUseCase resolveAlert;
    private final SseEmitterRegistry sseEmitterRegistry;

    public AlertController(AlertRepository alertRepository,
                           AcknowledgeAlertUseCase acknowledgeAlert,
                           ResolveAlertUseCase resolveAlert,
                           SseEmitterRegistry sseEmitterRegistry) {
        this.alertRepository = alertRepository;
        this.acknowledgeAlert = acknowledgeAlert;
        this.resolveAlert = resolveAlert;
        this.sseEmitterRegistry = sseEmitterRegistry;
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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseEmitterRegistry.register();
    }
}
