package com.example.sportsbook.sports;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.market.api.dto.CreateMarketRequest;
import com.example.sportsbook.market.api.dto.CreateSelectionRequest;
import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.MarketType;
import com.example.sportsbook.sports.api.dto.CreateEventRequest;
import com.example.sportsbook.sports.api.dto.EventResponse;
import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.repository.SportRepository;
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
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SportsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SportRepository sportRepository;

    private String adminToken;
    private Long footballId;

    @BeforeEach
    void setUp() throws Exception {
        // Find existing football sport (created by migration)
        footballId = sportRepository.findByName("Football").map(Sport::getId)
                .orElseGet(() -> sportRepository.save(Sport.builder().name("Football").build()).getId());

        // Login as admin to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("password");
        
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        adminToken = loginResponse.getToken();
    }

    @Test
    void shouldCreateEventMarketAndSelectionAndVerifyVisibility() throws Exception {
        // 1. Create Event
        CreateEventRequest createEventRequest = new CreateEventRequest(
                footballId,
                "Real Madrid vs Barcelona",
                "Real Madrid",
                "Barcelona",
                OffsetDateTime.now().plusDays(1)
        );

        String eventJson = mockMvc.perform(post("/api/admin/sports/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Real Madrid vs Barcelona"))
                .andReturn().getResponse().getContentAsString();

        EventResponse eventResponse = objectMapper.readValue(eventJson, EventResponse.class);
        Long eventId = eventResponse.id();

        // 2. Create Market
        CreateMarketRequest createMarketRequest = new CreateMarketRequest(
                eventId,
                MarketType.MATCH_RESULT,
                MarketStatus.OPEN
        );

        String marketJson = mockMvc.perform(post("/api/admin/sports/markets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMarketRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marketType").value("MATCH_RESULT"))
                .andReturn().getResponse().getContentAsString();

        MarketResponse marketResponse = objectMapper.readValue(marketJson, MarketResponse.class);
        Long marketId = marketResponse.id();

        // 3. Create Selection
        CreateSelectionRequest createSelectionRequest = new CreateSelectionRequest(
                marketId,
                "Real Madrid to win",
                new BigDecimal("1.85")
        );

        mockMvc.perform(post("/api/admin/sports/selections")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSelectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Real Madrid to win"))
                .andExpect(jsonPath("$.odds").value(1.85));

        // 4. Verify Visibility (Public API)
        mockMvc.perform(get("/api/sports/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.id == " + eventId + ")].name").value("Real Madrid vs Barcelona"))
                .andExpect(jsonPath("$[?(@.id == " + eventId + ")].markets[0].marketType").value("MATCH_RESULT"))
                .andExpect(jsonPath("$[?(@.id == " + eventId + ")].markets[0].selections[0].name").value("Real Madrid to win"));
    }

    @Test
    void shouldDenyEventCreationForAnonymousUser() throws Exception {
        CreateEventRequest createEventRequest = new CreateEventRequest(
                footballId,
                "Forbidden Event",
                "Home",
                "Away",
                OffsetDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/api/admin/sports/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest)))
                .andExpect(status().isUnauthorized());
    }
}
