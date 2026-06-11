package com.example.sportsbook;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SportsbookApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    private String loginAndGetToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().getToken();
    }

    private HttpHeaders getAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void healthCheckReturnsUpWithoutAuth() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/api/public/health", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void meEndpointReturnsCurrentUser() {
        String token = loginAndGetToken("player@example.com", "password");
        HttpEntity<Void> requestEntity = new HttpEntity<>(getAuthHeaders(token));

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/me",
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("email")).isEqualTo("player@example.com");
    }

    @Test
    void adminEndpointAccessibleByAdmin() {
        String token = loginAndGetToken("admin@example.com", "password");
        HttpEntity<Void> requestEntity = new HttpEntity<>(getAuthHeaders(token));

        ResponseEntity<List> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/admin/users",
                HttpMethod.GET,
                requestEntity,
                List.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isGreaterThan(0);
    }

    @Test
    void adminEndpointForbiddenForPlayer() {
        String token = loginAndGetToken("player@example.com", "password");
        HttpEntity<Void> requestEntity = new HttpEntity<>(getAuthHeaders(token));

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/admin/users",
                HttpMethod.GET,
                requestEntity,
                Map.class
        );
        // Method security returns 403 Forbidden
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void registrationWorksWithValidData() {
        RegistrationRequest request = RegistrationRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(20))
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("email")).isEqualTo("newuser@example.com");
    }

    @Test
    void registrationFailsForUnderage() {
        RegistrationRequest request = RegistrationRequest.builder()
                .email("kid@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(10))
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
