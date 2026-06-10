package com.example.sportsbook;

import com.example.sportsbook.user.api.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Test
    void healthCheckReturnsUpWithoutAuth() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/api/public/health", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void meEndpointReturnsCurrentUser() {
        ResponseEntity<Map> response = restTemplate.withBasicAuth("player@example.com", "password")
                .getForEntity("http://localhost:" + port + "/api/users/me", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("email")).isEqualTo("player@example.com");
    }

    @Test
    void adminEndpointAccessibleByAdmin() {
        ResponseEntity<List> response = restTemplate.withBasicAuth("admin@example.com", "password")
                .getForEntity("http://localhost:" + port + "/api/admin/users", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isGreaterThan(0);
    }

    @Test
    void adminEndpointForbiddenForPlayer() {
        ResponseEntity<Map> response = restTemplate.withBasicAuth("player@example.com", "password")
                .getForEntity("http://localhost:" + port + "/api/admin/users", Map.class);
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
