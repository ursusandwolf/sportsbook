package com.example.sportsbook.settlement.service;

import com.example.sportsbook.audit.service.AuditService;
import com.example.sportsbook.betting.model.Bet;
import com.example.sportsbook.betting.model.BetStatus;
import com.example.sportsbook.betting.repository.BetRepository;
import com.example.sportsbook.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BetRepository betRepository;
    private final WalletService walletService;
    private final AuditService auditService;

    @Transactional
    public void settle(Long betId, BetStatus outcome) {
        Bet bet = betRepository.findById(betId)
                .orElseThrow(() -> new IllegalArgumentException("Bet not found: " + betId));

        if (isAlreadySettledWithSameOutcome(bet, outcome)) {
            return;
        }
        assertBetIsAccepted(bet);

        applyWalletSettlement(bet, outcome, "settle-" + betId + "-" + outcome);

        bet.setStatus(outcome);
        bet.setSettledAt(OffsetDateTime.now());
        betRepository.save(bet);

        auditService.log("BET_SETTLEMENT", "BET", betId.toString(), "Status: " + outcome);
    }

    private boolean isAlreadySettledWithSameOutcome(Bet bet, BetStatus outcome) {
        return bet.getStatus() != BetStatus.ACCEPTED && bet.getStatus() == outcome;
    }

    private void assertBetIsAccepted(Bet bet) {
        if (bet.getStatus() != BetStatus.ACCEPTED) {
            throw new IllegalStateException("Bet is already settled with status: " + bet.getStatus());
        }
    }

    private void applyWalletSettlement(Bet bet, BetStatus outcome, String idempotencyKey) {
        Long userId = bet.getUser().getId();
        String betId = bet.getId().toString();

        switch (outcome) {
            case WON -> walletService.settleWin(
                    userId, bet.getStake(), bet.getPotentialPayout(), "BET", betId, idempotencyKey);
            case LOST -> walletService.settleLoss(userId, bet.getStake(), "BET", betId, idempotencyKey);
            case VOID, CANCELLED -> walletService.settleRefund(userId, bet.getStake(), "BET", betId, idempotencyKey);
            default -> throw new IllegalArgumentException("Invalid settlement outcome: " + outcome);
        }
    }
}
