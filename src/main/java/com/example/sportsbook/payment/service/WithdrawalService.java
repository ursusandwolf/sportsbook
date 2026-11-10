package com.example.sportsbook.payment.service;

import com.example.sportsbook.audit.service.AuditService;
import com.example.sportsbook.fraud.model.FraudOperation;
import com.example.sportsbook.fraud.service.FraudContext;
import com.example.sportsbook.fraud.service.FraudEngine;
import com.example.sportsbook.fraud.service.RiskAssessmentService;
import com.example.sportsbook.payment.model.Withdrawal;
import com.example.sportsbook.payment.model.WithdrawalStatus;
import com.example.sportsbook.payment.repository.WithdrawalRepository;
import com.example.sportsbook.user.model.KycStatus;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.model.UserStatus;
import com.example.sportsbook.wallet.exception.InsufficientFundsException;
import com.example.sportsbook.wallet.model.Wallet;
import com.example.sportsbook.wallet.repository.WalletRepository;
import com.example.sportsbook.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private static final int MONEY_SCALE = 2;
    private static final int MAX_DESTINATION_LENGTH = 255;

    private final WithdrawalRepository withdrawalRepository;
    private final WalletRepository walletRepository;
    private final FraudEngine fraudEngine;
    private final RiskAssessmentService riskAssessmentService;
    private final AuditService auditService;
    private final WalletService walletService;

    @Transactional
    public Withdrawal requestWithdrawal(User user, BigDecimal amount, String destinationFingerprint) {
        BigDecimal withdrawalAmount = requireValidAmount(amount);
        requireValidDestination(destinationFingerprint);
        assertUserCanRequestWithdrawal(user);
        assertSufficientFunds(user.getId(), withdrawalAmount);

        runWithdrawalFraudChecks(user.getId());
        WithdrawalStatus status = resolveStatusFromRisk(user.getId());

        Withdrawal withdrawal = withdrawalRepository.save(Withdrawal.builder()
                .user(user)
                .amount(withdrawalAmount)
                .destinationFingerprint(destinationFingerprint)
                .status(status)
                .createdAt(OffsetDateTime.now())
                .build());

        walletService.reserveWithdrawal(
                user.getId(),
                withdrawalAmount,
                withdrawal.getId().toString(),
                "WITHDRAWAL_" + withdrawal.getId()
        );

        auditService.log(
                "WITHDRAWAL_REQUESTED",
                "PLAYER",
                user.getId().toString(),
                "User " + user.getId() + " requested withdrawal of " + withdrawalAmount
        );

        return withdrawal;
    }

    private BigDecimal requireValidAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        try {
            return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Withdrawal amount must have at most two decimal places");
        }
    }

    private void requireValidDestination(String destinationFingerprint) {
        if (destinationFingerprint == null
                || destinationFingerprint.isBlank()
                || destinationFingerprint.length() > MAX_DESTINATION_LENGTH) {
            throw new IllegalArgumentException("A valid withdrawal destination is required");
        }
    }

    private void assertUserCanRequestWithdrawal(User user) {
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.SELF_EXCLUDED) {
            throw new IllegalStateException("User account cannot request withdrawals");
        }
        if (user.getKycStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException("KYC not verified");
        }
    }

    private void assertSufficientFunds(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }

    private void runWithdrawalFraudChecks(Long userId) {
        fraudEngine.run(FraudContext.builder()
                .userId(userId)
                .operation(FraudOperation.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private WithdrawalStatus resolveStatusFromRisk(Long userId) {
        return switch (riskAssessmentService.assess(userId).getDecision()) {
            case ALLOW, ALLOW_WITH_MONITORING, BLOCK_BONUS -> WithdrawalStatus.PENDING;
            case REQUIRE_ADDITIONAL_VERIFICATION, HOLD_WITHDRAWAL, MANUAL_REVIEW -> WithdrawalStatus.HELD_FOR_REVIEW;
            case RESTRICT_ACCOUNT, REJECT -> throw new IllegalStateException("Withdrawal is not permitted");
        };
    }
}
