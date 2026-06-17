package com.example.sportsbook.bonus;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.bonus.model.BonusCampaign;
import com.example.sportsbook.bonus.repository.BonusCampaignRepository;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BonusIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BonusCampaignRepository bonusCampaignRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String playerToken;
    private Long playerId;
    private Long campaignId;

    @BeforeEach
    void setUp() {
        // Setup Admin
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@example.com");
        adminLogin.setPassword("password");
        ResponseEntity<LoginResponse> adminResp = restTemplate.postForEntity("/api/auth/login", adminLogin, LoginResponse.class);
        adminToken = adminResp.getBody().getToken();

        // Setup Player
        String playerEmail = "player_" + UUID.randomUUID() + "@example.com";
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .email(playerEmail)
                .password("Password123!")
                .confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(25))
                .build();
        restTemplate.postForEntity("/api/auth/register", regRequest, Void.class);
        
        playerId = userRepository.findByEmail(playerEmail).get().getId();

        LoginRequest playerLogin = new LoginRequest();
        playerLogin.setEmail(playerEmail);
        playerLogin.setPassword("Password123!");
        ResponseEntity<LoginResponse> playerResp = restTemplate.postForEntity("/api/auth/login", playerLogin, LoginResponse.class);
        playerToken = playerResp.getBody().getToken();

        // Setup Campaign
        BonusCampaign campaign = bonusCampaignRepository.save(BonusCampaign.builder()
                .name("Welcome Bonus")
                .type("DEPOSIT_MATCH")
                .startAt(java.time.OffsetDateTime.now())
                .build());
        campaignId = campaign.getId();
    }

    @Test
    void testIssueBonus() {
        // Admin issues bonus
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", playerId.toString());
        params.add("campaignId", campaignId.toString());
        params.add("amount", "50.00");
        params.add("wagering", "100.00");
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("/api/admin/bonuses/issue", request, Void.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Player checks balance
        HttpHeaders playerHeaders = new HttpHeaders();
        playerHeaders.setBearerAuth(playerToken);
        ResponseEntity<BigDecimal> balanceResp = restTemplate.exchange("/api/v1/bonuses/me/balance", HttpMethod.GET, new HttpEntity<>(playerHeaders), BigDecimal.class);
        
        assertThat(balanceResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(balanceResp.getBody()).isEqualByComparingTo("50.00");
    }
}
