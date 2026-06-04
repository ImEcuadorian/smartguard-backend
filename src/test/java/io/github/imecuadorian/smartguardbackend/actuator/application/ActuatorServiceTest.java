package io.github.imecuadorian.smartguardbackend.actuator.application;

import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorMapper;
import io.github.imecuadorian.smartguardbackend.actuator.api.CreateActuatorCommandRequest;
import io.github.imecuadorian.smartguardbackend.actuator.api.CreateActuatorRequest;
import io.github.imecuadorian.smartguardbackend.actuator.domain.Actuator;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandStatus;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorStatus;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorType;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorCommandRepository;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActuatorServiceTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID ACTUATOR_ID = UUID.fromString("e7f90b8d-1bf4-45aa-a2fd-5dcd290a98dc");

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ActuatorRepository actuatorRepository;

    @Mock
    private ActuatorCommandRepository commandRepository;

    private ActuatorService actuatorService;

    @BeforeEach
    void setUp() {
        actuatorService = new ActuatorService(deviceRepository, actuatorRepository, commandRepository, new ActuatorMapper());
    }

    @Test
    void createActuatorStoresAnActiveActuator() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var request = new CreateActuatorRequest(
                DEVICE_ID,
                "relay-door",
                "Door relay",
                ActuatorType.RELAY,
                "Main entrance"
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(actuatorRepository.existsByCode("relay-door")).thenReturn(false);
        when(actuatorRepository.save(any(Actuator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = actuatorService.create(request);

        assertThat(response.code()).isEqualTo("relay-door");
        assertThat(response.status()).isEqualTo(ActuatorStatus.ACTIVE);
    }

    @Test
    void createCommandStoresPendingCommand() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var actuator = new Actuator(device, "relay-door", "Door relay", ActuatorType.RELAY, "Main entrance");

        when(actuatorRepository.findById(ACTUATOR_ID)).thenReturn(Optional.of(actuator));
        when(commandRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = actuatorService.createCommand(ACTUATOR_ID, new CreateActuatorCommandRequest(
                ActuatorCommandType.OPEN_DOOR,
                "Open for authorized card"
        ));

        assertThat(response.command()).isEqualTo(ActuatorCommandType.OPEN_DOOR);
        assertThat(response.status()).isEqualTo(ActuatorCommandStatus.PENDING);
    }
}
