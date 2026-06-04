package io.github.imecuadorian.smartguardbackend.realtime.application;

import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorReadingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketRealtimeNotifierTest {

    @Test
    void sensorReadingCreatedPublishesToSensorAndDeviceTopics() {
        var template = mock(SimpMessagingTemplate.class);
        var notifier = new WebSocketRealtimeNotifier(template);
        var sensorId = UUID.fromString("aabdb5ce-9531-42e6-a43e-4368cc9dca9e");
        var deviceId = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
        var reading = new SensorReadingResponse(
                UUID.fromString("89cb0388-61f0-4c68-b0c2-6dcfdc74ab80"),
                sensorId,
                deviceId,
                new BigDecimal("25.6"),
                null,
                null,
                Instant.parse("2026-06-03T20:00:00Z"),
                null
        );

        notifier.sensorReadingCreated(reading);

        verify(template).convertAndSend("/topic/sensors/" + sensorId + "/readings", reading);
        verify(template).convertAndSend("/topic/devices/" + deviceId + "/readings", reading);
    }
}
