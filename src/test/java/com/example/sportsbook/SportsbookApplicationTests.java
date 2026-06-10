package com.example.sportsbook;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

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
    void profileEndpointRequiresAuth() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/api/profile", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void profileEndpointWorksWithCorrectAuth() {
        ResponseEntity<Map> response = restTemplate.withBasicAuth("player", "password")
                .getForEntity("http://localhost:" + port + "/api/profile", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("username")).isEqualTo("player");
    }

    @Test
    void profileEndpointFailsWithIncorrectAuth() {
        ResponseEntity<Map> response = restTemplate.withBasicAuth("player", "wrong")
                .getForEntity("http://localhost:" + port + "/api/profile", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
