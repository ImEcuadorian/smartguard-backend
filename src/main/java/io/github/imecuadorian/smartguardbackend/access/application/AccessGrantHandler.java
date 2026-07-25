package io.github.imecuadorian.smartguardbackend.access.application;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;

public interface AccessGrantHandler {
    void handleGrantedAccess(AccessEvent event);

    static AccessGrantHandler noop() {
        return event -> {
        };
    }
}
