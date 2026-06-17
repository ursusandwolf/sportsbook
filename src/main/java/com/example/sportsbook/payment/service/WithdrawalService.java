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
import com.example.sportsbook.wallet.model.Wallet;
import com.example.sportsbook.wallet.repository.WalletRepository;
import com.example.sportsbook.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final WalletRepository walletRepository;
    private final FraudEngine fraudEngine;
    private final RiskAssessmentService riskAssessmentService;
    private final AuditService auditService;
    private final WalletService walletService;

    @Transactional
    public Withdrawal requestWithdrawal(User user, BigDecimal amount, String destinationFingerprint) {
        // 1. Business Validation
        if (user.getKycStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException("KYC not verified");
        }
        
        // Check sufficient funds (wallet logic)
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        // 2. Fraud Check
        FraudContext context = FraudContext.builder()
                .userId(user.getId())
                .operation(FraudOperation.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .build();
        fraudEngine.run(context);
        
        // 3. Risk Assessment (simplified for this iteration)
        // In reality, we'd use riskAssessmentService.assess(context)
        
        Withdrawal withdrawal = Withdrawal.builder()
                .user(user)
                .amount(amount)
                .destinationFingerprint(destinationFingerprint)
                .status(WithdrawalStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .version(0L)
                .build();
        
        withdrawalRepository.save(withdrawal);
        
        // Deduct from wallet
        walletService.reserveFunds(user.getId(), amount, "WITHDRAWAL", withdrawal.getId().toString(), "WITHDRAWAL_" + withdrawal.getId());
        
        auditService.log("WITHDRAWAL_REQUESTED", "PLAYER", user.getId().toString(),
                "User " + user.getId() + " requested withdrawal of " + amount);
        
        return withdrawal;
    }
}
