package com.slamonitor.alert.infrastructure.persistence;

import com.slamonitor.alert.domain.model.EndpointInfo;
import com.slamonitor.alert.domain.port.EndpointQueryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Reads endpoint metadata from the shared {@code endpoints} table (owned by ingestor-service).
 */
@Repository
public class EndpointQueryJdbcAdapter implements EndpointQueryRepository {

    private static final String SQL = """
            SELECT id, url, http_method
            FROM endpoints
            WHERE active = true
            """;

    private final JdbcTemplate jdbc;

    public EndpointQueryJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<EndpointInfo> findAllActive() {
        return jdbc.query(SQL, (rs, rowNum) -> new EndpointInfo(
                rs.getObject("id", UUID.class),
                rs.getString("url"),
                rs.getString("http_method")
        ));
    }
}
