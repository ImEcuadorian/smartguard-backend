package io.github.imecuadorian.smartguardbackend;

import io.github.imecuadorian.smartguardbackend.access.infrastructure.AccessEventRepository;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.AccessReaderRepository;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.RfidCardRepository;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorCommandRepository;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorRepository;
import io.github.imecuadorian.smartguardbackend.alert.infrastructure.AlertRepository;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorReadingRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorRepository;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.RefreshTokenRepository;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
        "smartguard.jpa-auditing.enabled=false",
        "spring.flyway.enabled=false",
        "smartguard.mqtt.enabled=false",
        "smartguard.monitoring.silence-monitor.enabled=false"
})
class SmartguardBackendApplicationTests {

    @MockitoBean
    private DeviceRepository deviceRepository;

    @MockitoBean
    private SensorRepository sensorRepository;

    @MockitoBean
    private SensorReadingRepository sensorReadingRepository;

    @MockitoBean
    private SensorAlertRuleRepository sensorAlertRuleRepository;

    @MockitoBean
    private AccessReaderRepository accessReaderRepository;

    @MockitoBean
    private RfidCardRepository rfidCardRepository;

    @MockitoBean
    private AccessEventRepository accessEventRepository;

    @MockitoBean
    private ActuatorRepository actuatorRepository;

    @MockitoBean
    private ActuatorCommandRepository actuatorCommandRepository;

    @MockitoBean
    private AlertRepository alertRepository;

    @MockitoBean
    private UserAccountRepository userAccountRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void contextLoads() {
    }

}
