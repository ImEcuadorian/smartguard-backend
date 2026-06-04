package io.github.imecuadorian.smartguardbackend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@ConditionalOnProperty(name = "smartguard.jpa-auditing.enabled", havingValue = "true", matchIfMissing = true)
public class PersistenceConfig {
}
