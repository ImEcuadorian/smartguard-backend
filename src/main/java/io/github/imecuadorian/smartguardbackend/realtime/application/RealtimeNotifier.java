package io.github.imecuadorian.smartguardbackend.realtime.application;

import io.github.imecuadorian.smartguardbackend.access.api.AccessEventResponse;
import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorCommandResponse;
import io.github.imecuadorian.smartguardbackend.alert.api.AlertResponse;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorReadingResponse;

public interface RealtimeNotifier {
    void sensorReadingCreated(SensorReadingResponse reading);

    void accessEventCreated(AccessEventResponse event);

    void actuatorCommandCreated(ActuatorCommandResponse command);

    void alertChanged(AlertResponse alert);

    static RealtimeNotifier noop() {
        return new RealtimeNotifier() {
            @Override
            public void sensorReadingCreated(SensorReadingResponse reading) {
            }

            @Override
            public void accessEventCreated(AccessEventResponse event) {
            }

            @Override
            public void actuatorCommandCreated(ActuatorCommandResponse command) {
            }

            @Override
            public void alertChanged(AlertResponse alert) {
            }
        };
    }
}
