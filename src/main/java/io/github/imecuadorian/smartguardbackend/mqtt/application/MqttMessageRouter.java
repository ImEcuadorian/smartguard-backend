package io.github.imecuadorian.smartguardbackend.mqtt.application;

import io.github.imecuadorian.smartguardbackend.access.api.AccessScanRequest;
import io.github.imecuadorian.smartguardbackend.access.application.AccessService;
import io.github.imecuadorian.smartguardbackend.device.application.DeviceService;
import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorReadingRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorService;
import io.github.imecuadorian.smartguardbackend.mqtt.api.MqttAccessEventPayload;
import io.github.imecuadorian.smartguardbackend.mqtt.api.MqttSensorReadingPayload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class MqttMessageRouter {

    private final ObjectMapper objectMapper;
    private final SensorService sensorService;
    private final AccessService accessService;
    private final DeviceService deviceService;

    public MqttMessageRouter(ObjectMapper objectMapper,
                             SensorService sensorService,
                             AccessService accessService,
                             DeviceService deviceService) {
        this.objectMapper = objectMapper;
        this.sensorService = sensorService;
        this.accessService = accessService;
        this.deviceService = deviceService;
    }

    public void route(String topic, String payload) {
        try {
            if (topic.endsWith("/readings")) {
                handleReading(topic, payload);
                return;
            }
            if (topic.endsWith("/access-events")) {
                handleAccessEvent(topic, payload);
                return;
            }
            throw new IllegalArgumentException("Unsupported MQTT topic: " + topic);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid MQTT payload for topic: " + topic, exception);
        }
    }

    private void handleReading(String topic, String payload) throws Exception {
        var message = objectMapper.readValue(payload, MqttSensorReadingPayload.class);
        authenticateDevice(topic, message.deviceCode(), message.deviceApiKey());
        sensorService.createReadingByCode(message.sensorCode(), new CreateSensorReadingRequest(
                message.numericValue(),
                message.booleanValue(),
                message.textValue(),
                message.recordedAt()
        ));
    }

    private void handleAccessEvent(String topic, String payload) throws Exception {
        var message = objectMapper.readValue(payload, MqttAccessEventPayload.class);
        authenticateDevice(topic, message.deviceCode(), message.deviceApiKey());
        accessService.scan(new AccessScanRequest(
                message.readerCode(),
                message.cardUid(),
                message.occurredAt()
        ));
    }

    private void authenticateDevice(String topic, String payloadDeviceCode, String deviceApiKey) {
        String topicDeviceCode = deviceCodeFromTopic(topic);
        String deviceCode = payloadDeviceCode == null || payloadDeviceCode.isBlank()
                ? topicDeviceCode
                : payloadDeviceCode.trim();

        if (topicDeviceCode != null && !topicDeviceCode.equals(deviceCode)) {
            throw new IllegalArgumentException("MQTT device code does not match topic");
        }

        deviceService.authenticateDevice(deviceCode, deviceApiKey);
    }

    private String deviceCodeFromTopic(String topic) {
        String marker = "/devices/";
        int markerIndex = topic.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }

        int start = markerIndex + marker.length();
        int end = topic.indexOf('/', start);
        if (end < 0 || start == end) {
            return null;
        }
        return topic.substring(start, end);
    }
}
