package com.slamonitor.alert.domain.port;

import com.slamonitor.alert.domain.model.SlaRuleInfo;

import java.util.List;

public interface SlaRuleQueryRepository {
    List<SlaRuleInfo> findAllEnabled();
}
