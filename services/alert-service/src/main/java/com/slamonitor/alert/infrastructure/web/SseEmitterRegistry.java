package com.slamonitor.alert.infrastructure.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.port.AlertStreamPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages active SSE connections and broadcasts new alerts to all connected clients.
 * Implements {@link AlertStreamPublisher} so the application layer stays decoupled
 * from the web infrastructure.
 */
@Component
@SuppressWarnings("null")
public class SseEmitterRegistry implements AlertStreamPublisher {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public SseEmitterRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter register() {
        var emitter = new SseEmitter(0L); // no server-side timeout; client reconnects on drop
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        log.debug("SSE client connected — {} active streams", emitters.size());
        return emitter;
    }

    @Override
    public void publish(Alert alert) {
        if (emitters.isEmpty()) return;

        String json;
        try {
            json = objectMapper.writeValueAsString(AlertResponse.from(alert));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize alert {} for SSE broadcast", alert.getId(), e);
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("alert").data(json));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
