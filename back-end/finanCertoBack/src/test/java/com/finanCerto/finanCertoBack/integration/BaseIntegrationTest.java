package com.finanCerto.finanCertoBack.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configura H2 em memória para testes de integração
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false"); // Desabilita Flyway para H2
        registry.add("spring.h2.console.enabled", () -> "true");
        
        // Configurações JWT para testes
        registry.add("jwt.secret", () -> "test-secret-key-for-integration-tests-must-be-at-least-256-bits");
        registry.add("jwt.expiration", () -> "86400000"); // 24 horas
        
        // Habilita validação de Bean Validation
        registry.add("spring.jpa.properties.javax.persistence.validation.mode", () -> "auto");
        registry.add("spring.jpa.properties.hibernate.validator.apply_to_ddl", () -> "true");
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
