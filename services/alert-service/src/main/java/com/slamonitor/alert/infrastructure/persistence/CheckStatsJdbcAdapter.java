package com.slamonitor.alert.infrastructure.persistence;

import com.slamonitor.alert.domain.model.CheckStats;
import com.slamonitor.alert.domain.port.CheckStatsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reads aggregated check data from the shared {@code checks} table (owned by sla-processor).
 * Uses JdbcTemplate to avoid defining a JPA entity for a table owned by another service.
 */
@Repository
public class CheckStatsJdbcAdapter implements CheckStatsRepository {

    private static final String SQL = """
            SELECT
                endpoint_id,
                COUNT(*)                                                              AS total_checks,
                SUM(CASE WHEN success THEN 1 ELSE 0 END)                             AS successful_checks,
                PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY latency_ms)
                    FILTER (WHERE latency_ms IS NOT NULL)                             AS p95_latency_ms
            FROM checks
            WHERE checked_at >= ? AND checked_at < ?
            GROUP BY endpoint_id
            """;

    private final JdbcTemplate jdbc;

    public CheckStatsJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CheckStats> summarize(Instant from, Instant to) {
        return jdbc.query(SQL,
                (rs, rowNum) -> new CheckStats(
                        rs.getObject("endpoint_id", UUID.class),
                        rs.getLong("total_checks"),
                        rs.getLong("successful_checks"),
                        rs.getObject("p95_latency_ms") != null ? rs.getDouble("p95_latency_ms") : null
                ),
                Timestamp.from(from), Timestamp.from(to));
    }
}
