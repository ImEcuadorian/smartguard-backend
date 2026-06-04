package io.github.imecuadorian.smartguardbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartguardBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartguardBackendApplication.class, args);
    }

}
