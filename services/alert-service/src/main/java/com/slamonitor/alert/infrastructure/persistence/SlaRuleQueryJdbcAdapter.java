package com.slamonitor.alert.infrastructure.persistence;

import com.slamonitor.alert.domain.model.SlaRuleInfo;
import com.slamonitor.alert.domain.port.SlaRuleQueryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Reads SLA rule definitions from the shared {@code sla_rules} table (owned by ingestor-service).
 */
@Repository
public class SlaRuleQueryJdbcAdapter implements SlaRuleQueryRepository {

    private static final String SQL = """
            SELECT id, endpoint_id, rule_type, threshold_value, threshold_unit,
                   window_seconds, sla_target, severity
            FROM sla_rules
            WHERE enabled = true
            """;

    private final JdbcTemplate jdbc;

    public SlaRuleQueryJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SlaRuleInfo> findAllEnabled() {
        return jdbc.query(SQL, (rs, rowNum) -> new SlaRuleInfo(
                rs.getObject("id", UUID.class),
                rs.getObject("endpoint_id", UUID.class),
                rs.getString("rule_type"),
                rs.getBigDecimal("threshold_value"),
                rs.getString("threshold_unit"),
                rs.getInt("window_seconds"),
                rs.getBigDecimal("sla_target"),
                rs.getString("severity")
        ));
    }
}
