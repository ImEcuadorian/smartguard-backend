package io.github.imecuadorian.smartguardbackend.actuator.application;

import io.github.imecuadorian.smartguardbackend.access.application.AccessGrantHandler;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;
import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorMapper;
import io.github.imecuadorian.smartguardbackend.actuator.domain.Actuator;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorCommandRepository;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorRepository;
import io.github.imecuadorian.smartguardbackend.realtime.application.RealtimeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UnlockOnGrantedAccessHandler implements AccessGrantHandler {

    private static final Logger log = LoggerFactory.getLogger(UnlockOnGrantedAccessHandler.class);

    private final ActuatorRepository actuatorRepository;
    private final ActuatorCommandRepository commandRepository;
    private final ActuatorCommandPublisher commandPublisher;
    private final ActuatorMapper actuatorMapper;
    private final RealtimeNotifier realtimeNotifier;
    private final boolean enabled;
    private final String lockActuatorCode;
    private final int durationMs;

    public UnlockOnGrantedAccessHandler(
            ActuatorRepository actuatorRepository,
            ActuatorCommandRepository commandRepository,
            ActuatorCommandPublisher commandPublisher,
            ActuatorMapper actuatorMapper,
            RealtimeNotifier realtimeNotifier,
            @Value("${smartguard.access.auto-unlock.enabled:true}") boolean enabled,
            @Value("${smartguard.access.auto-unlock.actuator-code:lock-main}") String lockActuatorCode,
            @Value("${smartguard.access.auto-unlock.duration-ms:1000}") int durationMs
    ) {
        this.actuatorRepository = actuatorRepository;
        this.commandRepository = commandRepository;
        this.commandPublisher = commandPublisher;
        this.actuatorMapper = actuatorMapper;
        this.realtimeNotifier = realtimeNotifier;
        this.enabled = enabled;
        this.lockActuatorCode = lockActuatorCode;
        this.durationMs = durationMs;
    }

    @Override
    public void handleGrantedAccess(AccessEvent event) {
        if (!enabled) {
            return;
        }

        actuatorRepository.findByCode(lockActuatorCode).ifPresentOrElse(
                actuator -> publishUnlockCommand(event, actuator),
                () -> log.warn("Auto unlock skipped: actuator code '{}' was not found", lockActuatorCode)
        );
    }

    private void publishUnlockCommand(AccessEvent event, Actuator actuator) {
        if (!actuator.getDevice().getCode().equals(event.getDevice().getCode())) {
            log.warn(
                    "Auto unlock skipped: actuator '{}' belongs to device '{}' but access reader belongs to '{}'",
                    actuator.getCode(),
                    actuator.getDevice().getCode(),
                    event.getDevice().getCode()
            );
            return;
        }

        var command = commandRepository.save(new ActuatorCommand(
                actuator,
                ActuatorCommandType.UNLOCK,
                "{\"durationMs\":" + durationMs + "}"
        ));

        try {
            commandPublisher.publish(command);
            realtimeNotifier.actuatorCommandCreated(actuatorMapper.toResponse(command));
        } catch (Exception exception) {
            log.error(
                    "Auto unlock command could not be published | actuatorCode={} | accessEventId={}",
                    actuator.getCode(),
                    event.getId(),
                    exception
            );
        }
    }
}
