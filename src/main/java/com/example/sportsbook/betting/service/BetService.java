package com.example.sportsbook.betting.service;

import com.example.sportsbook.betting.api.dto.BetResponse;
import com.example.sportsbook.betting.api.dto.PlaceBetRequest;
import com.example.sportsbook.betting.model.Bet;
import com.example.sportsbook.betting.model.BetStatus;
import com.example.sportsbook.betting.repository.BetRepository;
import com.example.sportsbook.common.service.OutboxService;
import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.Selection;
import com.example.sportsbook.market.repository.SelectionRepository;
import com.example.sportsbook.responsiblegaming.service.ResponsibleGamingService;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.model.UserStatus;
import com.example.sportsbook.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BetService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final BetRepository betRepository;
    private final SelectionRepository selectionRepository;
    private final WalletService walletService;
    private final ResponsibleGamingService responsibleGamingService;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    public BetResponse placeBet(User user, PlaceBetRequest request) {
        return betRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(this::mapToResponse)
                .orElseGet(() -> createBet(user, request));
    }

    private BetResponse createBet(User user, PlaceBetRequest request) {
        assertUserActive(user);

        BigDecimal stake = request.stake().setScale(MONEY_SCALE, MONEY_ROUNDING);
        responsibleGamingService.validateStake(user.getId(), stake);

        Selection selection = loadOpenSelection(request);
        assertOddsUnchanged(selection, request.expectedOdds());

        String idempotencyKey = request.idempotencyKey();
        // Persist first so ledger referenceId is the real bet id; reserve failure rolls the bet back.
        Bet savedBet = betRepository.save(buildAcceptedBet(user, selection, stake, idempotencyKey));
        walletService.reserveFunds(
                user.getId(),
                stake,
                "BET",
                savedBet.getId().toString(),
                "BET_RESERVE_" + idempotencyKey
        );
        publishBetPlacedEvent(savedBet, user.getId());
        return mapToResponse(savedBet);
    }

    private void assertUserActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User account is not active: " + user.getStatus());
        }
    }

    private Selection loadOpenSelection(PlaceBetRequest request) {
        Selection selection = selectionRepository.findById(request.selectionId())
                .orElseThrow(() -> new IllegalArgumentException("Selection not found"));

        if (selection.getMarket().getStatus() != MarketStatus.OPEN) {
            throw new IllegalStateException("Market is not open: " + selection.getMarket().getStatus());
        }
        return selection;
    }

    private void assertOddsUnchanged(Selection selection, BigDecimal expectedOdds) {
        if (selection.getOdds().compareTo(expectedOdds) != 0) {
            throw new IllegalStateException(
                    "Odds have changed. Expected: " + expectedOdds + ", Current: " + selection.getOdds());
        }
    }

    private Bet buildAcceptedBet(User user, Selection selection, BigDecimal stake, String idempotencyKey) {
        BigDecimal potentialPayout = stake.multiply(selection.getOdds()).setScale(MONEY_SCALE, MONEY_ROUNDING);
        return Bet.builder()
                .user(user)
                .selection(selection)
                .stake(stake)
                .oddsAtPlacement(selection.getOdds())
                .potentialPayout(potentialPayout)
                .status(BetStatus.ACCEPTED)
                .placedAt(OffsetDateTime.now())
                .idempotencyKey(idempotencyKey)
                .build();
    }

    private void publishBetPlacedEvent(Bet bet, Long userId) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "betId", bet.getId(),
                    "userId", userId,
                    "stake", bet.getStake()
            ));
            outboxService.saveEvent("BET_PLACED", payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }

    private BetResponse mapToResponse(Bet bet) {
        return new BetResponse(
                bet.getId(),
                bet.getSelection().getId(),
                bet.getSelection().getName(),
                bet.getStake(),
                bet.getOddsAtPlacement(),
                bet.getPotentialPayout(),
                bet.getStatus(),
                bet.getPlacedAt()
        );
    }
}
