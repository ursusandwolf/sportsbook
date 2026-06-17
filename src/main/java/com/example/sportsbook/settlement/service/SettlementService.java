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

        if (bet.getStatus() != BetStatus.ACCEPTED) {
            if (bet.getStatus() == outcome) {
                return; // Idempotency: already settled with this outcome
            }
            throw new IllegalStateException("Bet is already settled with status: " + bet.getStatus());
        }

        String idempotencyKey = "settle-" + betId + "-" + outcome;

        switch (outcome) {
            case WON -> walletService.settleWin(bet.getUser().getId(), bet.getStake(), bet.getPotentialPayout(), "BET", betId.toString(), idempotencyKey);
            case LOST -> walletService.settleLoss(bet.getUser().getId(), bet.getStake(), "BET", betId.toString(), idempotencyKey);
            case VOID, CANCELLED -> walletService.settleRefund(bet.getUser().getId(), bet.getStake(), "BET", betId.toString(), idempotencyKey);
            default -> throw new IllegalArgumentException("Invalid settlement outcome: " + outcome);
        }

        bet.setStatus(outcome);
        bet.setSettledAt(OffsetDateTime.now());
        betRepository.save(bet);

        auditService.log("BET_SETTLEMENT", "BET", betId.toString(), "Status: " + outcome);
    }
}
