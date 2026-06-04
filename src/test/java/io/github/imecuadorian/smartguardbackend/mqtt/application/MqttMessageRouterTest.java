package io.github.imecuadorian.smartguardbackend.mqtt.application;

import io.github.imecuadorian.smartguardbackend.access.api.AccessScanRequest;
import io.github.imecuadorian.smartguardbackend.access.application.AccessService;
import io.github.imecuadorian.smartguardbackend.device.application.DeviceAuthenticationException;
import io.github.imecuadorian.smartguardbackend.device.application.DeviceService;
import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqttMessageRouterTest {

    @Test
    void routesAccessEventPayloadToAccessService() {
        var sensorService = mock(SensorService.class);
        var accessService = mock(AccessService.class);
        var deviceService = mock(DeviceService.class);
        var router = new MqttMessageRouter(new JsonMapper(), sensorService, accessService, deviceService);

        router.route("smartguard/devices/esp32-001/access-events",
                "{\"deviceCode\":\"esp32-001\",\"deviceApiKey\":\"device-api-key\",\"readerCode\":\"rfid-main\",\"cardUid\":\"A1:B2:C3:D4\"}");

        verify(deviceService).authenticateDevice("esp32-001", "device-api-key");
        verify(accessService).scan(any(AccessScanRequest.class));
    }

    @Test
    void rejectsReadingPayloadWhenDeviceCredentialsAreInvalid() {
        var sensorService = mock(SensorService.class);
        var accessService = mock(AccessService.class);
        var deviceService = mock(DeviceService.class);
        var router = new MqttMessageRouter(new JsonMapper(), sensorService, accessService, deviceService);

        when(deviceService.authenticateDevice("esp32-001", "bad-key"))
                .thenThrow(new DeviceAuthenticationException("Invalid device credentials"));

        assertThatThrownBy(() -> router.route("smartguard/devices/esp32-001/readings",
                "{\"deviceCode\":\"esp32-001\",\"deviceApiKey\":\"bad-key\",\"sensorCode\":\"gas-main\",\"numericValue\":25.5}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid MQTT payload for topic: smartguard/devices/esp32-001/readings")
                .hasCauseInstanceOf(DeviceAuthenticationException.class);
    }
}
