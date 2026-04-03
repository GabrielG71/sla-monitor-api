package com.slamonitor.alert.application.usecase;

import com.slamonitor.alert.domain.model.Alert;
import com.slamonitor.alert.domain.model.CheckStats;
import com.slamonitor.alert.domain.model.EndpointSlaReport;
import com.slamonitor.alert.domain.model.RuleCompliance;
import com.slamonitor.alert.domain.model.SlaReport;
import com.slamonitor.alert.domain.model.SlaRuleInfo;
import com.slamonitor.alert.domain.port.AlertRepository;
import com.slamonitor.alert.domain.port.CheckStatsRepository;
import com.slamonitor.alert.domain.port.EndpointQueryRepository;
import com.slamonitor.alert.domain.port.SlaRuleQueryRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GenerateSlaReportUseCase {

    private final CheckStatsRepository checkStatsRepository;
    private final SlaRuleQueryRepository slaRuleQueryRepository;
    private final EndpointQueryRepository endpointQueryRepository;
    private final AlertRepository alertRepository;

    public GenerateSlaReportUseCase(CheckStatsRepository checkStatsRepository,
                                    SlaRuleQueryRepository slaRuleQueryRepository,
                                    EndpointQueryRepository endpointQueryRepository,
                                    AlertRepository alertRepository) {
        this.checkStatsRepository = checkStatsRepository;
        this.slaRuleQueryRepository = slaRuleQueryRepository;
        this.endpointQueryRepository = endpointQueryRepository;
        this.alertRepository = alertRepository;
    }

    public SlaReport execute(YearMonth month) {
        Instant from = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to   = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<UUID, CheckStats> statsById = checkStatsRepository.summarize(from, to).stream()
                .collect(Collectors.toMap(CheckStats::endpointId, s -> s));

        Map<UUID, com.slamonitor.alert.domain.model.EndpointInfo> endpointsById =
                endpointQueryRepository.findAllActive().stream()
                        .collect(Collectors.toMap(com.slamonitor.alert.domain.model.EndpointInfo::id, e -> e));

        Map<UUID, List<SlaRuleInfo>> rulesByEndpoint = slaRuleQueryRepository.findAllEnabled().stream()
                .collect(Collectors.groupingBy(SlaRuleInfo::endpointId));

        Map<UUID, List<Alert>> alertsByRule = alertRepository.findByTriggeredAtBetween(from, to).stream()
                .collect(Collectors.groupingBy(Alert::getSlaRuleId));

        List<EndpointSlaReport> endpointReports = new ArrayList<>();
        for (var entry : rulesByEndpoint.entrySet()) {
            UUID endpointId = entry.getKey();
            var endpoint = endpointsById.get(endpointId);
            if (endpoint == null) continue;

            CheckStats stats = statsById.getOrDefault(endpointId,
                    new CheckStats(endpointId, 0L, 0L, null));

            List<RuleCompliance> compliances = entry.getValue().stream()
                    .map(rule -> buildCompliance(rule, stats, alertsByRule.getOrDefault(rule.id(), List.of())))
                    .toList();

            endpointReports.add(new EndpointSlaReport(
                    endpointId, endpoint.url(),
                    stats.totalChecks(), stats.successfulChecks(), stats.availabilityPct(),
                    compliances));
        }

        return new SlaReport(month.toString(), Instant.now(), endpointReports);
    }

    private RuleCompliance buildCompliance(SlaRuleInfo rule, CheckStats stats, List<Alert> ruleAlerts) {
        long incidentCount = ruleAlerts.size();
        long downtimeMinutes = ruleAlerts.stream()
                .filter(a -> a.getResolvedAt() != null)
                .mapToLong(a -> Duration.between(a.getTriggeredAt(), a.getResolvedAt()).toMinutes())
                .sum();

        double measured;
        String unit;
        boolean compliant;

        switch (rule.ruleType()) {
            case "AVAILABILITY" -> {
                measured = stats.availabilityPct();
                unit = "PERCENT";
                compliant = rule.slaTarget() == null || measured >= rule.slaTarget().doubleValue();
            }
            case "LATENCY" -> {
                measured = stats.p95LatencyMs() != null ? stats.p95LatencyMs() : 0.0;
                unit = "MS";
                compliant = measured == 0.0 || measured <= rule.thresholdValue().doubleValue();
            }
            case "ERROR_RATE" -> {
                measured = stats.errorRatePct();
                unit = "PERCENT";
                compliant = measured <= rule.thresholdValue().doubleValue();
            }
            default -> {
                measured = 0.0;
                unit = "UNKNOWN";
                compliant = true;
            }
        }

        return new RuleCompliance(rule.id(), rule.ruleType(), rule.slaTarget(),
                rule.thresholdValue(), rule.thresholdUnit(),
                measured, unit, compliant, incidentCount, downtimeMinutes);
    }
}
