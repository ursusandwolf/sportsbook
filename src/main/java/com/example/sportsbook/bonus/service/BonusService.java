package com.example.sportsbook.bonus.service;

import com.example.sportsbook.audit.service.AuditService;
import com.example.sportsbook.bonus.model.BonusCampaign;
import com.example.sportsbook.bonus.model.UserBonus;
import com.example.sportsbook.bonus.repository.BonusCampaignRepository;
import com.example.sportsbook.bonus.repository.UserBonusRepository;
import com.example.sportsbook.fraud.model.FraudOperation;
import com.example.sportsbook.fraud.service.FraudContext;
import com.example.sportsbook.fraud.service.FraudEngine;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.wallet.model.Wallet;
import com.example.sportsbook.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusCampaignRepository bonusCampaignRepository;
    private final UserBonusRepository userBonusRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;
    private final FraudEngine fraudEngine;

    @Transactional
    public UserBonus issueBonus(User user, Long campaignId, BigDecimal amount, BigDecimal wageringRequirement) {
        // Run fraud check
        FraudContext context = FraudContext.builder()
                .userId(user.getId())
                .operation(FraudOperation.BONUS_CLAIM)
                .timestamp(LocalDateTime.now())
                .build();
        fraudEngine.run(context);

        // Note: For this iteration, we don't block the bonus yet. 
        // We'll add blocking logic in a later iteration.

        BonusCampaign campaign = bonusCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        UserBonus userBonus = UserBonus.builder()
                .user(user)
                .campaign(campaign)
                .amount(amount)
                .remainingWagering(wageringRequirement)
                .status("ACTIVE")
                .build();
        userBonusRepository.save(userBonus);

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        wallet.setBonusBalance(wallet.getBonusBalance().add(amount));
        walletRepository.save(wallet);

        auditService.log("BONUS_ISSUED", "ADMIN", "SYSTEM", 
                "User " + user.getId() + " received bonus " + amount + " from campaign " + campaign.getName());
        
        return userBonus;
    }

    @Transactional(readOnly = true)
    public BigDecimal getActiveBonusBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBonusBalance)
                .orElse(BigDecimal.ZERO);
    }
}
