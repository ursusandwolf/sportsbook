package com.example.sportsbook.responsiblegaming;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.betting.api.dto.PlaceBetRequest;
import com.example.sportsbook.market.model.Market;
import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.MarketType;
import com.example.sportsbook.market.model.Selection;
import com.example.sportsbook.market.repository.MarketRepository;
import com.example.sportsbook.market.repository.SelectionRepository;
import com.example.sportsbook.responsiblegaming.api.dto.SelfExclusionRequest;
import com.example.sportsbook.responsiblegaming.api.dto.UserLimitRequest;
import com.example.sportsbook.sports.model.EventStatus;
import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.model.SportEvent;
import com.example.sportsbook.sports.repository.SportEventRepository;
import com.example.sportsbook.sports.repository.SportRepository;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.wallet.api.dto.DepositRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ResponsibleGamingIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private SportEventRepository sportEventRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private SelectionRepository selectionRepository;

    private String token;
    private Long selectionId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        if (userEmail == null) {
            userEmail = "rg_test_" + UUID.randomUUID() + "@example.com";
            // 1. Register
            RegistrationRequest regRequest = RegistrationRequest.builder()
                    .email(userEmail)
                    .password("Password123!")
                    .confirmPassword("Password123!")
                    .dateOfBirth(LocalDate.now().minusYears(25))
                    .build();
            restTemplate.postForEntity("/api/auth/register", regRequest, Void.class);
        }

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(userEmail);
        loginRequest.setPassword("Password123!");
        
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
        token = loginResponse.getBody().getToken();

        // Setup a selection for betting
        if (selectionId == null) {
            Sport sport = sportRepository.findByName("Tennis")
                    .orElseGet(() -> sportRepository.save(Sport.builder().name("Tennis").build()));
            SportEvent event = sportEventRepository.save(SportEvent.builder()
                    .name("Federer vs Nadal")
                    .sport(sport)
                    .homeParticipant("Federer")
                    .awayParticipant("Nadal")
                    .startsAt(OffsetDateTime.now().plusDays(1))
                    .status(EventStatus.SCHEDULED)
                    .build());
            Market market = marketRepository.save(Market.builder()
                    .event(event)
                    .marketType(MarketType.MATCH_RESULT)
                    .status(MarketStatus.OPEN)
                    .build());
            Selection selection = selectionRepository.save(Selection.builder()
                    .market(market)
                    .name("Federer")
                    .odds(new BigDecimal("1.90"))
                    .build());
            selectionId = selection.getId();
        }
    }

    @Test
    void testDepositLimit() {
        // 1. Set daily deposit limit to 100
        UserLimitRequest limitRequest = new UserLimitRequest(
                new BigDecimal("100.00"), null, null, null, null, null
        );
        HttpEntity<UserLimitRequest> limitEntity = new HttpEntity<>(limitRequest, headers());
        ResponseEntity<Void> limitResponse = restTemplate.postForEntity("/api/v1/responsible-gaming/limits", limitEntity, Void.class);
        assertThat(limitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Deposit 60 (Allowed)
        DepositRequest d1 = new DepositRequest(new BigDecimal("60.00"), UUID.randomUUID().toString());
        ResponseEntity<Void> r1 = restTemplate.postForEntity("/api/wallets/deposit", new HttpEntity<>(d1, headers()), Void.class);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. Deposit 50 (Rejected: 60 + 50 = 110 > 100)
        DepositRequest d2 = new DepositRequest(new BigDecimal("50.00"), UUID.randomUUID().toString());
        ResponseEntity<Object> r2 = restTemplate.postForEntity("/api/wallets/deposit", new HttpEntity<>(d2, headers()), Object.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void testStakeLimit() {
        // 1. Set daily stake limit to 50
        UserLimitRequest limitRequest = new UserLimitRequest(
                null, null, null, new BigDecimal("50.00"), null, null
        );
        HttpEntity<UserLimitRequest> limitEntity = new HttpEntity<>(limitRequest, headers());
        restTemplate.postForEntity("/api/v1/responsible-gaming/limits", limitEntity, Void.class);

        // 2. Ensure enough funds
        DepositRequest d1 = new DepositRequest(new BigDecimal("200.00"), UUID.randomUUID().toString());
        restTemplate.postForEntity("/api/wallets/deposit", new HttpEntity<>(d1, headers()), Void.class);

        // 3. Place bet 30 (Allowed)
        PlaceBetRequest b1 = new PlaceBetRequest(selectionId, new BigDecimal("30.00"), new BigDecimal("1.90"));
        ResponseEntity<Void> r1 = restTemplate.postForEntity("/api/bets", new HttpEntity<>(b1, headers()), Void.class);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 4. Place bet 25 (Rejected: 30 + 25 = 55 > 50)
        PlaceBetRequest b2 = new PlaceBetRequest(selectionId, new BigDecimal("25.00"), new BigDecimal("1.90"));
        ResponseEntity<Object> r2 = restTemplate.postForEntity("/api/bets", new HttpEntity<>(b2, headers()), Object.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void testSelfExclusionEnforcement() {
        // 1. Apply self-exclusion
        SelfExclusionRequest seRequest = new SelfExclusionRequest(OffsetDateTime.now().plusDays(7), "Take a break");
        HttpEntity<SelfExclusionRequest> seEntity = new HttpEntity<>(seRequest, headers());
        ResponseEntity<Void> seResponse = restTemplate.postForEntity("/api/v1/responsible-gaming/self-exclude", seEntity, Void.class);
        assertThat(seResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Try to deposit (Rejected)
        DepositRequest d1 = new DepositRequest(new BigDecimal("10.00"), UUID.randomUUID().toString());
        ResponseEntity<Object> r1 = restTemplate.postForEntity("/api/wallets/deposit", new HttpEntity<>(d1, headers()), Object.class);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        // 3. Try to place bet (Rejected)
        PlaceBetRequest b1 = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), new BigDecimal("1.90"));
        ResponseEntity<Object> r2 = restTemplate.postForEntity("/api/bets", new HttpEntity<>(b1, headers()), Object.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
