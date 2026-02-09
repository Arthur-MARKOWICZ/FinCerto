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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public abstract class MultiServiceIntegrationTest {

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
        postgres.start();
        setupPythonApp();
    }

    private static void setupPythonApp() {
        try {
            pythonApp.start();
            pythonApp.execInContainer("apt-get", "update");
            pythonApp.execInContainer("apt-get", "install", "-y", "curl");
            pythonApp.execInContainer("pip", "install", "fastapi", "uvicorn", "sqlalchemy", "psycopg2-binary");
            setupPythonApplication();
        } catch (Exception e) {
            System.err.println("Erro ao configurar Python: " + e.getMessage());
        }
    }

    private static void setupPythonApplication() {
        try {
            // Cria um script Python simples diretamente no container
            pythonApp.execInContainer("sh", "-c", 
                "cat > /tmp/app.py << 'EOF'\n" +
                "from fastapi import FastAPI\n" +
                "from fastapi.middleware.cors import CORSMiddleware\n" +
                "import uvicorn\n" +
                "import psycopg2\n" +
                "from psycopg2.extras import RealDictCursor\n" +
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
                "def get_db_connection():\n" +
                "    return psycopg2.connect(\n" +
                "        host=\"host.testcontainers.internal\",\n" +
                "        port=5432,\n" +
                "        database=\"finanCerto_test\",\n" +
                "        user=\"test_user\",\n" +
                "        password=\"test_password\"\n" +
                "    )\n" +
                "\n" +
                "@app.get(\"/\")\n" +
                "def home():\n" +
                "    return {\"message\": \"Python API is running\"}\n" +
                "\n" +
                "@app.get(\"/test-db\")\n" +
                "def test_db():\n" +
                "    try:\n" +
                "        conn = get_db_connection()\n" +
                "        cursor = conn.cursor(cursor_factory=RealDictCursor)\n" +
                "        cursor.execute(\"SELECT NOW()\")\n" +
                "        result = cursor.fetchone()\n" +
                "        conn.close()\n" +
                "        return {\"db_time\": str(result[\"now\"])}\n" +
                "    except Exception as e:\n" +
                "        return {\"error\": str(e)}\n" +
                "\n" +
                "if __name__ == \"__main__\":\n" +
                "    uvicorn.run(app, host=\"0.0.0.0\", port=8001)\n" +
                "EOF"
            );
            
            // Executa a aplicação Python em background
            pythonApp.execInContainer("sh", "-c", "python /tmp/app.py &");
            
        } catch (Exception e) {
            System.err.println("Erro ao configurar aplicação Python: " + e.getMessage());
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
                // Aguarda e tenta novamente
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

    protected boolean waitForJavaApp() {
        int maxAttempts = 30;
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    getJavaAppUrl() + "/actuator/health", String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return true;
                }
            } catch (Exception e) {
                // Aguarda e tenta novamente
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

    protected void setupTestDatabase() {
        try {
            String createTableSql = "CREATE TABLE IF NOT EXISTS test_reports (" +
                    "id SERIAL PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "content TEXT," +
                    "created_at TIMESTAMP DEFAULT NOW()" +
                    ");";
            
            postgres.execInContainer("psql", "-U", "test_user", "-d", "finanCerto_test", "-c", createTableSql);
        } catch (Exception e) {
            System.err.println("Erro ao configurar banco de testes: " + e.getMessage());
        }
    }
}
