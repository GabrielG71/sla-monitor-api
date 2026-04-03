package com.slamonitor.alert.domain.port;

import com.slamonitor.alert.domain.model.Alert;

public interface WebhookDispatcher {
    void dispatch(Alert alert);
}
