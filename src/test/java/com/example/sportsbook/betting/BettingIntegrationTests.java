package com.example.sportsbook.betting;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.betting.api.dto.PlaceBetRequest;
import com.example.sportsbook.market.api.dto.CreateMarketRequest;
import com.example.sportsbook.market.api.dto.CreateSelectionRequest;
import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.market.api.dto.SelectionResponse;
import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.MarketType;
import com.example.sportsbook.sports.api.dto.CreateEventRequest;
import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.repository.SportRepository;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.wallet.api.dto.DepositRequest;
import com.example.sportsbook.wallet.api.dto.WalletResponse;
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
class BettingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SportRepository sportRepository;

    private String adminToken;
    private String playerToken;
    private Long selectionId;
    private final BigDecimal odds = new BigDecimal("2.0000");

    @BeforeEach
    void setUp() throws Exception {
        // 1. Get Admin Token
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@example.com");
        adminLogin.setPassword("password");
        adminToken = getToken(adminLogin);

        // 2. Setup Betting Line (as Admin)
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

        // 3. Register and Login Player
        String email = "player_" + UUID.randomUUID() + "@example.com";
        RegistrationRequest reg = RegistrationRequest.builder().email(email).password("Password123!").confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(25)).build();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reg)));

        LoginRequest playerLogin = new LoginRequest();
        playerLogin.setEmail(email);
        playerLogin.setPassword("Password123!");
        playerToken = getToken(playerLogin);

        // 4. Deposit Funds to Player Wallet
        DepositRequest deposit = new DepositRequest(new BigDecimal("100.00"), UUID.randomUUID().toString());
        mockMvc.perform(post("/api/wallets/deposit").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());
    }

    private String getToken(LoginRequest loginRequest) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, LoginResponse.class).getToken();
    }

    @Test
    void shouldPlaceBetSuccessfully() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), odds.setScale(4));

        mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stake").value(10.00))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.potentialPayout").value(20.00));

        // Verify wallet balance (100 - 10 = 90 available, 10 reserved)
        mockMvc.perform(get("/api/wallets/me").header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(90.00))
                .andExpect(jsonPath("$.reservedBalance").value(10.00));
    }

    @Test
    void shouldFailIfOddsChanged() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(selectionId, new BigDecimal("10.00"), new BigDecimal("1.50"));

        mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()); // IllegalStateException mapped to 422
    }

    @Test
    void shouldFailIfInsufficientFunds() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(selectionId, new BigDecimal("1000.00"), odds.setScale(4));

        mockMvc.perform(post("/api/bets").header("Authorization", "Bearer " + playerToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()); // InsufficientFundsException mapped to 422
    }
}
