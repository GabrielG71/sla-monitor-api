package com.slamonitor.alert.infrastructure.web;

import com.slamonitor.alert.application.usecase.GenerateSlaReportUseCase;
import com.slamonitor.alert.domain.model.SlaReport;
import com.slamonitor.alert.domain.port.AlertRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final GenerateSlaReportUseCase generateSlaReport;
    private final AlertRepository alertRepository;

    public ReportController(GenerateSlaReportUseCase generateSlaReport,
                            AlertRepository alertRepository) {
        this.generateSlaReport = generateSlaReport;
        this.alertRepository = alertRepository;
    }

    /**
     * Monthly SLA compliance report.
     *
     * @param month  YYYY-MM (defaults to the previous calendar month)
     * @param format "json" (default) or "csv"
     */
    @GetMapping("/sla")
    public ResponseEntity<?> slaReport(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "json") String format) {

        YearMonth ym = (month == null || month.isBlank())
                ? YearMonth.now().minusMonths(1)
                : YearMonth.parse(month);

        SlaReport report = generateSlaReport.execute(ym);

        if ("csv".equalsIgnoreCase(format)) {
            String csv = SlaReportCsvSerializer.toCsv(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"sla-report-" + ym + ".csv\"")
                    .body(csv);
        }

        return ResponseEntity.ok(report);
    }

    /**
     * Incident timeline: alerts for a given period, optionally filtered by endpoint.
     *
     * @param endpointId optional UUID filter
     * @param from       ISO-8601 start instant (inclusive)
     * @param to         ISO-8601 end instant (exclusive)
     */
    @GetMapping("/incidents")
    public List<IncidentResponse> incidents(
            @RequestParam(required = false) UUID endpointId,
            @RequestParam String from,
            @RequestParam String to) {

        Instant fromInstant = Instant.parse(from);
        Instant toInstant   = Instant.parse(to);

        var alerts = endpointId != null
                ? alertRepository.findByEndpointIdAndTriggeredAtBetween(endpointId, fromInstant, toInstant)
                : alertRepository.findByTriggeredAtBetween(fromInstant, toInstant);

        return alerts.stream().map(IncidentResponse::from).toList();
    }
}
