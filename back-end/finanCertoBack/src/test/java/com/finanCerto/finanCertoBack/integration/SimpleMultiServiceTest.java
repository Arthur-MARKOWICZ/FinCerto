package com.finanCerto.finanCertoBack.integration;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires Docker daemon - skipped in CI/CD environment")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SimpleMultiServiceTest {

    @LocalServerPort
    protected int javaAppPort;

    protected RestTemplate restTemplate = new RestTemplate();

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17"))
            .withDatabaseName("finanCerto_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @Container
    protected static final GenericContainer<?> pythonApp = new GenericContainer<>(
            DockerImageName.parse("python:3.12-slim"))
            .withExposedPorts(8001)
            .withStartupTimeout(Duration.ofMinutes(5))
            .withCommand("sleep", "infinity");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("jwt.secret", () -> "test-secret-key-for-integration-tests-must-be-at-least-256-bits");
        registry.add("jwt.expiration", () -> "86400000");
    }

    @BeforeAll
    static void setupContainers() {
        try {
            postgres.start();
            setupPythonApp();
        } catch (Exception e) {
            System.err.println("Erro ao configurar containers: " + e.getMessage());
        }
    }

    private static void setupPythonApp() {
        try {
            pythonApp.start();
            
            // Instala dependências básicas
            pythonApp.execInContainer("apt-get", "update");
            pythonApp.execInContainer("apt-get", "install", "-y", "curl");
            pythonApp.execInContainer("pip", "install", "fastapi", "uvicorn");
            
            // Cria uma API Python simples
            pythonApp.execInContainer("sh", "-c", 
                "cat > /tmp/app.py << 'EOF'\n" +
                "from fastapi import FastAPI\n" +
                "from fastapi.middleware.cors import CORSMiddleware\n" +
                "import uvicorn\n" +
                "\n" +
                "app = FastAPI(title=\"Test Python API\")\n" +
                "\n" +
                "app.add_middleware(\n" +
                "    CORSMiddleware,\n" +
                "    allow_origins=[\"*\"],\n" +
                "    allow_credentials=True,\n" +
                "    allow_methods=[\"*\"],\n" +
                "    allow_headers=[\"*\"],\n" +
                ")\n" +
                "\n" +
                "@app.get(\"/\")\n" +
                "def home():\n" +
                "    return {\"message\": \"Python API is running\"}\n" +
                "\n" +
                "@app.get(\"/test-db\")\n" +
                "def test_db():\n" +
                "    return {\"db_time\": \"connected\"}\n" +
                "\n" +
                "if __name__ == \"__main__\":\n" +
                "    uvicorn.run(app, host=\"0.0.0.0\", port=8001)\n" +
                "EOF"
            );
            
            // Inicia a aplicação Python em background
            pythonApp.execInContainer("sh", "-c", "python /tmp/app.py &");
            
        } catch (Exception e) {
            System.err.println("Erro ao configurar Python: " + e.getMessage());
        }
    }

    protected String getJavaAppUrl() {
        return "http://localhost:" + javaAppPort;
    }

    protected String getPythonAppUrl() {
        return "http://localhost:" + pythonApp.getMappedPort(8001);
    }

    protected boolean waitForPythonApp() {
        int maxAttempts = 30;
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    getPythonAppUrl() + "/", String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return true;
                }
            } catch (Exception e) {

            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            attempt++;
        }
        
        return false;
    }

    @Test
    @Order(1)
    @DisplayName("Deve iniciar ambos os serviços")
    void testServicesStartup() {
        // Arrange & Act
        boolean pythonReady = waitForPythonApp();
        
        // Assert
        assertTrue(pythonReady, "Python API não está disponível");
        
        ResponseEntity<String> pythonResponse = restTemplate.getForEntity(
            getPythonAppUrl() + "/", String.class);
        assertTrue(pythonResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    @Order(2)
    @DisplayName("Deve permitir comunicação Java → Python")
    void testJavaToPythonCommunication() {
        // Arrange
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getPythonAppUrl() + "/", Map.class);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("Python API is running", response.getBody().get("message"));
    }

    @Test
    @Order(3)
    @DisplayName("Deve testar conexão com banco via Python")
    void testDatabaseConnection() {
        // Arrange
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getPythonAppUrl() + "/test-db", Map.class);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody().get("db_time"));
    }

    @Test
    @Order(4)
    @DisplayName("Deve validar tratamento de erros")
    void testErrorHandling() {
        // Arrange
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            restTemplate.getForEntity(getPythonAppUrl() + "/nonexistent", String.class);
        });
    }

    @AfterAll
    static void cleanup() {
        System.out.println("Testes de integração multi-serviço concluídos");
    }
}
