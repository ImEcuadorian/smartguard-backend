package io.github.imecuadorian.smartguardbackend.actuator.application;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReader;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderType;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessResult;
import io.github.imecuadorian.smartguardbackend.access.domain.RfidCard;
import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorMapper;
import io.github.imecuadorian.smartguardbackend.actuator.domain.Actuator;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorType;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorCommandRepository;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.realtime.application.RealtimeNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnlockOnGrantedAccessHandlerTest {

    @Mock
    private ActuatorRepository actuatorRepository;

    @Mock
    private ActuatorCommandRepository commandRepository;

    @Mock
    private ActuatorCommandPublisher commandPublisher;

    @Mock
    private RealtimeNotifier realtimeNotifier;

    @Test
    void grantedAccessPublishesUnlockCommandForConfiguredLock() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var reader = new AccessReader(device, "rfid-main", AccessReaderType.RFID_RC522, "Main entrance");
        var card = new RfidCard("7A:2E:44:32", "Hugo");
        var event = new AccessEvent(reader, card, "7A:2E:44:32", AccessResult.GRANTED, "Card authorized",
                Instant.parse("2026-07-22T13:00:00Z"));
        var actuator = new Actuator(device, "lock-main", "Door lock", ActuatorType.SOLENOID_LOCK, "Main entrance");

        when(actuatorRepository.findByCode("lock-main")).thenReturn(Optional.of(actuator));
        when(commandRepository.save(any(ActuatorCommand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var handler = new UnlockOnGrantedAccessHandler(
                actuatorRepository,
                commandRepository,
                commandPublisher,
                new ActuatorMapper(),
                realtimeNotifier,
                true,
                "lock-main",
                1000
        );

        handler.handleGrantedAccess(event);

        var commandCaptor = ArgumentCaptor.forClass(ActuatorCommand.class);
        verify(commandRepository).save(commandCaptor.capture());
        verify(commandPublisher).publish(any(ActuatorCommand.class));
        verify(realtimeNotifier).actuatorCommandCreated(any());

        var command = commandCaptor.getValue();
        assertThat(command.getActuator().getCode()).isEqualTo("lock-main");
        assertThat(command.getCommand()).isEqualTo(ActuatorCommandType.UNLOCK);
        assertThat(command.getPayload()).isEqualTo("{\"durationMs\":1000}");
    }

    @Test
    void disabledAutoUnlockDoesNotPublishCommand() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var reader = new AccessReader(device, "rfid-main", AccessReaderType.RFID_RC522, "Main entrance");
        var card = new RfidCard("7A:2E:44:32", "Hugo");
        var event = new AccessEvent(reader, card, "7A:2E:44:32", AccessResult.GRANTED, "Card authorized",
                Instant.parse("2026-07-22T13:00:00Z"));

        var handler = new UnlockOnGrantedAccessHandler(
                actuatorRepository,
                commandRepository,
                commandPublisher,
                new ActuatorMapper(),
                realtimeNotifier,
                false,
                "lock-main",
                1000
        );

        handler.handleGrantedAccess(event);

        verifyNoInteractions(actuatorRepository, commandRepository, commandPublisher, realtimeNotifier);
    }
}
