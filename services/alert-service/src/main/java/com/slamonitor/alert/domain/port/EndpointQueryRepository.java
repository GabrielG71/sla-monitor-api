package com.slamonitor.alert.domain.port;

import com.slamonitor.alert.domain.model.EndpointInfo;

import java.util.List;

public interface EndpointQueryRepository {
    List<EndpointInfo> findAllActive();
}
