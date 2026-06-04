package io.github.imecuadorian.smartguardbackend.realtime.application;

import io.github.imecuadorian.smartguardbackend.access.api.AccessEventResponse;
import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorCommandResponse;
import io.github.imecuadorian.smartguardbackend.alert.api.AlertResponse;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorReadingResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketRealtimeNotifier implements RealtimeNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketRealtimeNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sensorReadingCreated(SensorReadingResponse reading) {
        messagingTemplate.convertAndSend("/topic/sensors/" + reading.sensorId() + "/readings", reading);
        messagingTemplate.convertAndSend("/topic/devices/" + reading.deviceId() + "/readings", reading);
    }

    @Override
    public void accessEventCreated(AccessEventResponse event) {
        messagingTemplate.convertAndSend("/topic/access/events", event);
        messagingTemplate.convertAndSend("/topic/devices/" + event.deviceId() + "/access-events", event);
    }

    @Override
    public void actuatorCommandCreated(ActuatorCommandResponse command) {
        messagingTemplate.convertAndSend("/topic/actuators/" + command.actuatorId() + "/commands", command);
        messagingTemplate.convertAndSend("/topic/devices/" + command.deviceId() + "/commands", command);
    }

    @Override
    public void alertChanged(AlertResponse alert) {
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        if (alert.deviceId() != null) {
            messagingTemplate.convertAndSend("/topic/devices/" + alert.deviceId() + "/alerts", alert);
        }
    }
}
