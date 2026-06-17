package com.example.sportsbook.fraud;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.fraud.repository.DeviceRepository;
import com.example.sportsbook.fraud.repository.UserDeviceLinkRepository;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DeviceIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserDeviceLinkRepository userDeviceLinkRepository;

    private String playerToken;

    @BeforeEach
    void setUp() {
        String playerEmail = "player_" + UUID.randomUUID() + "@example.com";
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .email(playerEmail)
                .password("Password123!")
                .confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(25))
                .build();
        restTemplate.postForEntity("/api/auth/register", regRequest, Void.class);

        LoginRequest playerLogin = new LoginRequest();
        playerLogin.setEmail(playerEmail);
        playerLogin.setPassword("Password123!");
        ResponseEntity<LoginResponse> playerResp = restTemplate.postForEntity("/api/auth/login", playerLogin, LoginResponse.class);
        playerToken = playerResp.getBody().getToken();

        userDeviceLinkRepository.deleteAll();
        deviceRepository.deleteAll();
    }

    @Test
    void testDeviceCapturedOnAuthenticatedRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(playerToken);
        headers.set("User-Agent", "Mozilla/5.0 (TestDevice)");
        headers.set("Accept-Language", "en-US");

        restTemplate.exchange("/api/v1/bonuses/me/balance", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(deviceRepository.count()).isEqualTo(1);
        assertThat(userDeviceLinkRepository.count()).isEqualTo(1);
    }
}
