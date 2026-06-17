package com.example.sportsbook.settlement;

import com.example.sportsbook.audit.repository.AuditEventRepository;
import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.betting.api.dto.BetResponse;
import com.example.sportsbook.betting.api.dto.PlaceBetRequest;
import com.example.sportsbook.betting.model.BetStatus;
import com.example.sportsbook.market.api.dto.CreateMarketRequest;
import com.example.sportsbook.market.api.dto.CreateSelectionRequest;
import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.market.api.dto.SelectionResponse;
import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.MarketType;
import com.example.sportsbook.settlement.api.dto.SettlementRequest;
import com.example.sportsbook.sports.api.dto.CreateEventRequest;
import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.repository.SportRepository;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.wallet.api.dto.DepositRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SettlementIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private String adminToken;
    private String playerToken;
    private Long selectionId;
    private final BigDecimal odds = new BigDecimal("2.5000");

    @BeforeEach
    void setUp() throws Exception {
        // Setup admin, player, sport, event, market, selection, deposit (similar to BettingIntegrationTests)
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@example.com");
        adminLogin.setPassword("password");
        adminToken = getToken(adminLogin);

        Long sportId = sportRepository.findByName("Football").map(Sport::getId).orElseThrow();
        CreateEventRequest eventReq = new CreateEventRequest(sportId, "Test Event", "Home", "Away", OffsetDateTime.now().plusDays(1));
        String eventJson = mockMvc.perform(post("/api/admin/sports/events").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eventReq)))
                .andReturn().getResponse().getContentAsString();
        Long eventId = objectMapper.readValue(eventJson, EventResponse.class).id();

        CreateMarketRequest marketReq = new CreateMarketRequest(eventId, MarketType.MATCH_RESULT, MarketStatus.OPEN);
        String marketJson = mockMvc.perform(post("/api/admin/sports/markets").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(marketReq)))
                .andReturn().getResponse().getContentAsString();
        Long marketId = objectMapper.readValue(marketJson, MarketResponse.class).id();

        CreateSelectionRequest selReq = new CreateSelectionRequest(marketId, "Home Win", odds.setScale(4));
        String selJson = mockMvc.perform(post("/api/admin/sports/selections").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(selReq)))
                .andReturn().getResponse().getContentAsString();
        selectionId = objectMapper.readValue(selJson, SelectionResponse.class).id();

        String email = "player_" + UUID.randomUUID() + "@example.com";
        RegistrationRequest reg = RegistrationRequest.builder().email(email).password("Password123!").confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(25)).build();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reg)));

        LoginRequest playerLogin = new LoginRequest();
        playerLogin.setEmail(email);
        playerLogin.setPassword("Password123!");
        playerToken = getToken(playerLogin);

        DepositRequest deposit = new DepositRequest(new BigDecimal("100.00"), UUID.randomUUID().toString());
        mockMvc.perform(post("/api/wallets/deposit").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(deposit)));
    }

    private String getToken(LoginRequest loginRequest) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, LoginResponse.class).getToken();
    }

    @Test
    void shouldSettleWinSuccessfully() throws Exception {
        // 1. Place bet
        PlaceBetRequest placeReq = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), odds.setScale(4));
        String betJson = mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(placeReq)))
                .andReturn().getResponse().getContentAsString();
        Long betId = objectMapper.readValue(betJson, BetResponse.class).id();

        // 2. Settle WON
        SettlementRequest settleReq = new SettlementRequest(BetStatus.WON);
        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk());

        // 3. Verify Wallet (100 - 10 + 25 = 115 available, 0 reserved)
        mockMvc.perform(get("/api/wallets/me").header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(115.00))
                .andExpect(jsonPath("$.reservedBalance").value(0.00));

        // 4. Verify Audit Log
        long count = auditEventRepository.findAll().stream()
                .filter(e -> "BET_SETTLEMENT".equals(e.getAction()) && betId.toString().equals(e.getSubjectId()))
                .count();
        org.junit.jupiter.api.Assertions.assertEquals(1, count);
    }

    @Test
    void shouldSettleLossSuccessfully() throws Exception {
        // 1. Place bet
        PlaceBetRequest placeReq = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), odds.setScale(4));
        String betJson = mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(placeReq)))
                .andReturn().getResponse().getContentAsString();
        Long betId = objectMapper.readValue(betJson, BetResponse.class).id();

        // 2. Settle LOST
        SettlementRequest settleReq = new SettlementRequest(BetStatus.LOST);
        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk());

        // 3. Verify Wallet (100 - 10 = 90 available, 0 reserved)
        mockMvc.perform(get("/api/wallets/me").header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(90.00))
                .andExpect(jsonPath("$.reservedBalance").value(0.00));
    }

    @Test
    void shouldSettleRefundSuccessfully() throws Exception {
        // 1. Place bet
        PlaceBetRequest placeReq = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), odds.setScale(4));
        String betJson = mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(placeReq)))
                .andReturn().getResponse().getContentAsString();
        Long betId = objectMapper.readValue(betJson, BetResponse.class).id();

        // 2. Settle VOID
        SettlementRequest settleReq = new SettlementRequest(BetStatus.VOID);
        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk());

        // 3. Verify Wallet (100 - 10 + 10 = 100 available, 0 reserved)
        mockMvc.perform(get("/api/wallets/me").header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(100.00))
                .andExpect(jsonPath("$.reservedBalance").value(0.00));
    }

    @Test
    void shouldBeIdempotent() throws Exception {
        PlaceBetRequest placeReq = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), odds.setScale(4));
        String betJson = mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(placeReq)))
                .andReturn().getResponse().getContentAsString();
        Long betId = objectMapper.readValue(betJson, BetResponse.class).id();

        SettlementRequest settleReq = new SettlementRequest(BetStatus.WON);
        
        // First call
        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk());

        // Second call (should be no-op)
        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk());

        // Verify Wallet hasn't double-paid (still 115)
        mockMvc.perform(get("/api/wallets/me").header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(115.00));
    }

    @Test
    void shouldFailIfAlreadySettledWithDifferentStatus() throws Exception {
        PlaceBetRequest placeReq = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), odds.setScale(4));
        String betJson = mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(placeReq)))
                .andReturn().getResponse().getContentAsString();
        Long betId = objectMapper.readValue(betJson, BetResponse.class).id();

        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new SettlementRequest(BetStatus.WON))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/bets/" + betId + "/settle").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new SettlementRequest(BetStatus.LOST))))
                .andExpect(status().isUnprocessableEntity()); // IllegalStateException mapped to 422
    }

    @Test
    void shouldFailIfUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/bets/1/settle").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new SettlementRequest(BetStatus.WON))))
                .andExpect(status().isForbidden());
    }
}
