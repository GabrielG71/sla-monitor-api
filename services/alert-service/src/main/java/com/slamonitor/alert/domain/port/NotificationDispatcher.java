package com.slamonitor.alert.domain.port;

import com.slamonitor.alert.domain.model.Alert;

public interface NotificationDispatcher {
    void dispatch(Alert alert);
    boolean isEnabled();
}
