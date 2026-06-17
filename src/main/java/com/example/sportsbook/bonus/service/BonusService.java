package com.example.sportsbook.bonus.service;

import com.example.sportsbook.audit.service.AuditService;
import com.example.sportsbook.bonus.model.BonusCampaign;
import com.example.sportsbook.bonus.model.UserBonus;
import com.example.sportsbook.bonus.repository.BonusCampaignRepository;
import com.example.sportsbook.bonus.repository.UserBonusRepository;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.wallet.model.Wallet;
import com.example.sportsbook.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusCampaignRepository bonusCampaignRepository;
    private final UserBonusRepository userBonusRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;

    @Transactional
    public UserBonus issueBonus(User user, Long campaignId, BigDecimal amount, BigDecimal wageringRequirement) {
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
