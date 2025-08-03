package com.daniyalmlik.deployguard;

import com.daniyalmlik.deployguard.users.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("ci")
@Testcontainers
class DeployguardApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("deployguard.encryption-key", () -> "dGVzdC1rZXktMzItYnl0ZXMtZm9yLXRlc3Rpbmch");
        registry.add("deployguard.jwt.secret", () -> "dGVzdC1qd3Qtc2VjcmV0LWZvci1jaS10ZXN0aW5nLXdoaWNoLWlzLWxvbmctZW5vdWdo");
    }

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorHealthReturnsUp() {
        ResponseEntity<String> response = rest.getForEntity(url("/actuator/health"), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void meWithoutTokenReturns401() {
        ResponseEntity<String> response = rest.getForEntity(url("/api/me"), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void registerLoginAndMe() {
        Map<String, String> creds = Map.of("email", "admin@test.com", "password", "secret123");

        ResponseEntity<AuthResponse> register = rest.postForEntity(url("/api/auth/register"), creds, AuthResponse.class);
        assertThat(register.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<AuthResponse> login = rest.postForEntity(url("/api/auth/login"), creds, AuthResponse.class);
        assertThat(login.getStatusCode().value()).isEqualTo(200);
        assertThat(login.getBody()).isNotNull();
        String token = login.getBody().accessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> me = rest.exchange(url("/api/me"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(me.getStatusCode().value()).isEqualTo(200);
        assertThat(me.getBody()).contains("admin@test.com");
    }

    @Test
    void duplicateRegisterReturns409() {
        Map<String, String> creds = Map.of("email", "admin2@test.com", "password", "secret123");
        rest.postForEntity(url("/api/auth/register"), creds, String.class);
        ResponseEntity<String> second = rest.postForEntity(url("/api/auth/register"), creds, String.class);
        assertThat(second.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        Map<String, String> badCreds = Map.of("email", "nobody@test.com", "password", "wrongpassword");
        ResponseEntity<String> response = rest.postForEntity(url("/api/auth/login"), badCreds, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
