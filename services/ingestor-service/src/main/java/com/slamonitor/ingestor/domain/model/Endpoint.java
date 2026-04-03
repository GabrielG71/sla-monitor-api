package com.slamonitor.ingestor.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "endpoints")
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MonitoredService service;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> headers;

    @Column(name = "timeout_ms", nullable = false)
    private int timeoutMs;

    @Column(name = "interval_secs", nullable = false)
    private int intervalSecs;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Endpoint() {}

    public static Endpoint create(MonitoredService service, String url, String httpMethod,
                                  Map<String, String> headers, int timeoutMs, int intervalSecs) {
        var e = new Endpoint();
        e.service = service;
        e.url = url;
        e.httpMethod = httpMethod != null ? httpMethod : "GET";
        e.headers = headers;
        e.timeoutMs = timeoutMs;
        e.intervalSecs = intervalSecs;
        e.active = true;
        e.createdAt = Instant.now();
        return e;
    }

    public void update(String url, String httpMethod, Map<String, String> headers,
                       int timeoutMs, int intervalSecs) {
        this.url = url;
        this.httpMethod = httpMethod != null ? httpMethod : "GET";
        this.headers = headers;
        this.timeoutMs = timeoutMs;
        this.intervalSecs = intervalSecs;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() { return id; }
    public MonitoredService getService() { return service; }
    public String getUrl() { return url; }
    public String getHttpMethod() { return httpMethod; }
    public Map<String, String> getHeaders() { return headers; }
    public int getTimeoutMs() { return timeoutMs; }
    public int getIntervalSecs() { return intervalSecs; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
