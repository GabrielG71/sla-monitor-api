package com.slamonitor.alert.infrastructure.web;

import com.slamonitor.alert.domain.model.EndpointSlaReport;
import com.slamonitor.alert.domain.model.RuleCompliance;
import com.slamonitor.alert.domain.model.SlaReport;

/**
 * Serializes a {@link SlaReport} to RFC 4180-compliant CSV.
 * One row per (endpoint × rule) combination.
 */
class SlaReportCsvSerializer {

    private static final String HEADER =
            "month,endpoint_id,url,total_checks,successful_checks,availability_pct," +
            "rule_id,rule_type,sla_target,threshold_value,threshold_unit," +
            "measured_value,measured_unit,compliant,incident_count,downtime_minutes\n";

    static String toCsv(SlaReport report) {
        var sb = new StringBuilder(HEADER);
        for (EndpointSlaReport e : report.endpoints()) {
            for (RuleCompliance r : e.rules()) {
                sb.append(report.month()).append(',')
                  .append(e.endpointId()).append(',')
                  .append(quote(e.url())).append(',')
                  .append(e.totalChecks()).append(',')
                  .append(e.successfulChecks()).append(',')
                  .append(String.format("%.4f", e.availabilityPct())).append(',')
                  .append(r.ruleId()).append(',')
                  .append(r.ruleType()).append(',')
                  .append(r.slaTarget() != null ? r.slaTarget().toPlainString() : "").append(',')
                  .append(r.thresholdValue().toPlainString()).append(',')
                  .append(r.thresholdUnit()).append(',')
                  .append(String.format("%.4f", r.measuredValue())).append(',')
                  .append(r.measuredUnit()).append(',')
                  .append(r.compliant()).append(',')
                  .append(r.incidentCount()).append(',')
                  .append(r.downtimeMinutes()).append('\n');
            }
        }
        return sb.toString();
    }

    private static String quote(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
