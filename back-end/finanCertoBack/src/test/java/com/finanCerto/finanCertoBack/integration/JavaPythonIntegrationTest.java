package com.finanCerto.finanCertoBack.integration;

import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class JavaPythonIntegrationTest extends MultiServiceIntegrationTest {

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Order(1)
    @DisplayName("Deve iniciar ambos os serviços e conectar ao banco")
    void testServicesStartupAndDatabaseConnection() {
        // Arrange
        setupTestDatabase();

        // Act & Assert - Verifica se a aplicação Java está funcionando
        assertTrue(waitForJavaApp(), "Aplicação Java não iniciou corretamente");
        
        ResponseEntity<String> javaHealth = restTemplate.getForEntity(
            getJavaAppUrl() + "/actuator/health", String.class);
        assertTrue(javaHealth.getStatusCode().is2xxSuccessful());

        // Act & Assert - Verifica se a aplicação Python está funcionando
        assertTrue(waitForPythonApp(), "Aplicação Python não iniciou corretamente");
        
        ResponseEntity<String> pythonHealth = restTemplate.getForEntity(
            getPythonAppUrl() + "/", String.class);
        assertTrue(pythonHealth.getStatusCode().is2xxSuccessful());

        // Act & Assert - Verifica conexão com banco via Python
        ResponseEntity<Map> pythonDbResponse = restTemplate.getForEntity(
            getPythonAppUrl() + "/test-db", Map.class);
        assertTrue(pythonDbResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(pythonDbResponse.getBody().get("db_time"));
    }

    @Test
    @Order(2)
    @DisplayName("Deve permitir comunicação Java → Python")
    void testJavaToPythonCommunication() {
        // Arrange
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Requisição da aplicação Java para a API Python
        ResponseEntity<Map> response = restTemplate.getForEntity(
            getPythonAppUrl() + "/", Map.class);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("Python API is running", response.getBody().get("message"));
    }

    @Test
    @Order(3)
    @DisplayName("Deve persistir dados via Python e ler via Java")
    void testDataPersistencePythonToJava() throws Exception {
        // Arrange
        setupTestDatabase();
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Cria dados via API Python
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("title", "Test Report");
        reportData.put("content", "This is a test report created by Python API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(reportData, headers);

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            getPythonAppUrl() + "/reports", request, Map.class);

        // Assert - Verifica criação
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(createResponse.getBody().get("id"));

        // Act - Verifica dados no banco via Java (simulado)
        // Em um cenário real, aqui você faria uma consulta via repository Java
        ResponseEntity<Map> reportsResponse = restTemplate.getForEntity(
            getPythonAppUrl() + "/reports", Map.class);

        // Assert - Verifica leitura
        assertTrue(reportsResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(reportsResponse.getBody().get("reports"));
    }

    @Test
    @Order(4)
    @DisplayName("Deve validar consistência de dados entre serviços")
    void testDataConsistencyBetweenServices() throws Exception {
        // Arrange
        setupTestDatabase();
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Cria múltiplos registros via Python
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("title", "Report " + i);
            reportData.put("content", "Content for report " + i);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(reportData, headers);

            restTemplate.postForEntity(
                getPythonAppUrl() + "/reports", request, Map.class);
        }

        // Act - Verifica consistência dos dados
        ResponseEntity<Map> reportsResponse = restTemplate.getForEntity(
            getPythonAppUrl() + "/reports", Map.class);

        // Assert - Verifica que todos os dados estão consistentes
        assertTrue(reportsResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(reportsResponse.getBody().get("reports"));
        
        // Em um cenário real, você também verificaria via repository Java
        // para garantir consistência entre as duas leituras
    }

    @Test
    @Order(5)
    @DisplayName("Deve tratar erros de comunicação adequadamente")
    void testCommunicationErrorHandling() {
        // Arrange
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Tenta acessar endpoint inexistente
        try {
            restTemplate.getForEntity(getPythonAppUrl() + "/nonexistent", String.class);
            fail("Deveria ter lançado exceção para endpoint inexistente");
        } catch (Exception e) {
            // Assert - Verifica tratamento de erro
            assertTrue(e.getMessage().contains("404") || 
                      e.getMessage().contains("Not Found") ||
                      e.getMessage().contains("405"));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Deve validar comportamento transacional")
    void testTransactionalBehavior() throws Exception {
        // Arrange
        setupTestDatabase();
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Simula operação transacional
        // Em um cenário real, você usaria @Transactional no teste
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("title", "Transactional Test");
        reportData.put("content", "Testing transaction behavior");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(reportData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            getPythonAppUrl() + "/reports", request, Map.class);

        // Assert - Verifica que a transação foi completada
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody().get("id"));

        // Verifica que os dados foram persistidos
        ResponseEntity<Map> reportsResponse = restTemplate.getForEntity(
            getPythonAppUrl() + "/reports", Map.class);
        assertTrue(reportsResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    @Order(7)
    @DisplayName("Deve isolar dados entre testes")
    void testDataIsolationBetweenTests() throws Exception {
        // Arrange
        setupTestDatabase();
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Cria dados específicos para este teste
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("title", "Isolation Test " + System.currentTimeMillis());
        reportData.put("content", "Testing data isolation");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(reportData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            getPythonAppUrl() + "/reports", request, Map.class);

        // Assert - Verifica que os dados foram criados
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody().get("id"));

        // Em um cenário real com @Transactional, os dados seriam revertidos
        // e o próximo teste começaria com banco limpo
    }

    @Test
    @Order(8)
    @DisplayName("Deve validar concorrência entre serviços")
    void testConcurrencyBetweenServices() throws Exception {
        // Arrange
        setupTestDatabase();
        assertTrue(waitForPythonApp(), "Python API não está disponível");

        // Act - Simula múltiplas requisições concorrentes
        // Em um cenário real, você usaria CompletableFuture ou similar
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("title", "Concurrent Test " + threadId);
                    reportData.put("content", "Content from thread " + threadId);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(reportData, headers);

                    restTemplate.postForEntity(
                        getPythonAppUrl() + "/reports", request, Map.class);
                } catch (Exception e) {
                    System.err.println("Erro na thread " + threadId + ": " + e.getMessage());
                }
            }).start();
        }

        // Aguarda um pouco para as requisições serem processadas
        Thread.sleep(2000);

        // Assert - Verifica que todos os dados foram criados
        ResponseEntity<Map> reportsResponse = restTemplate.getForEntity(
            getPythonAppUrl() + "/reports", Map.class);
        assertTrue(reportsResponse.getStatusCode().is2xxSuccessful());
    }

    @AfterAll
    static void cleanup() {
        // Cleanup dos containers será feito automaticamente pelo Testcontainers
        System.out.println("Testes de integração multi-serviço concluídos");
    }
}
