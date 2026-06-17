package com.example.sportsbook.betting.service;

import com.example.sportsbook.betting.api.dto.BetResponse;
import com.example.sportsbook.betting.api.dto.PlaceBetRequest;
import com.example.sportsbook.betting.model.Bet;
import com.example.sportsbook.betting.model.BetStatus;
import com.example.sportsbook.betting.repository.BetRepository;
import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.Selection;
import com.example.sportsbook.market.repository.SelectionRepository;
import com.example.sportsbook.responsiblegaming.service.ResponsibleGamingService;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.model.UserStatus;
import com.example.sportsbook.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final SelectionRepository selectionRepository;
    private final WalletService walletService;
    private final ResponsibleGamingService responsibleGamingService;

    @Transactional
    public BetResponse placeBet(User user, PlaceBetRequest request) {
        // 1. Проверка статуса игрока
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User account is not active: " + user.getStatus());
        }

        // 1a. Responsible Gaming Check
        BigDecimal stake = request.stake().setScale(2, RoundingMode.HALF_UP);
        responsibleGamingService.validateStake(user.getId(), stake);

        // 2. Проверка исхода и рынка
        Selection selection = selectionRepository.findById(request.selectionId())
                .orElseThrow(() -> new IllegalArgumentException("Selection not found"));

        if (selection.getMarket().getStatus() != MarketStatus.OPEN) {
            throw new IllegalStateException("Market is not open: " + selection.getMarket().getStatus());
        }

        // 3. Сверка коэффициентов
        if (selection.getOdds().compareTo(request.expectedOdds()) != 0) {
            throw new IllegalStateException("Odds have changed. Expected: " + request.expectedOdds() + ", Current: " + selection.getOdds());
        }

        // 4. Резервирование средств (атомарно внутри транзакции)
        String idempotencyKey = "BET_RESERVE_" + UUID.randomUUID();
        
        // В реальной системе referenceId должен быть ID ставки, но ставка еще не создана.
        // Можно использовать временный UUID или сначала сохранить ставку в PENDING.
        // Для учебного проекта и упрощения: сначала резервируем, потом сохраняем ACCEPTED.
        walletService.reserveFunds(user.getId(), stake, "BET", "TEMP_" + idempotencyKey, idempotencyKey);

        BigDecimal potentialPayout = stake.multiply(selection.getOdds()).setScale(2, RoundingMode.HALF_UP);

        Bet bet = Bet.builder()
                .user(user)
                .selection(selection)
                .stake(stake)
                .oddsAtPlacement(selection.getOdds())
                .potentialPayout(potentialPayout)
                .status(BetStatus.ACCEPTED)
                .placedAt(OffsetDateTime.now())
                .build();

        Bet savedBet = betRepository.save(bet);

        return mapToResponse(savedBet);
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
