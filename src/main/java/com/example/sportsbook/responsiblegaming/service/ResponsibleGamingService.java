package com.example.sportsbook.responsiblegaming.service;

import com.example.sportsbook.audit.service.AuditService;
import com.example.sportsbook.responsiblegaming.model.SelfExclusion;
import com.example.sportsbook.responsiblegaming.model.UserLimit;
import com.example.sportsbook.responsiblegaming.repository.SelfExclusionRepository;
import com.example.sportsbook.responsiblegaming.repository.UserLimitRepository;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.model.UserStatus;
import com.example.sportsbook.user.repository.UserRepository;
import com.example.sportsbook.wallet.model.WalletEntryType;
import com.example.sportsbook.wallet.repository.WalletEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponsibleGamingService {

    private final UserLimitRepository userLimitRepository;
    private final SelfExclusionRepository selfExclusionRepository;
    private final WalletEntryRepository walletEntryRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public void validateDeposit(Long userId, BigDecimal amount) {
        Optional<UserLimit> limitsOpt = userLimitRepository.findByUserId(userId);
        if (limitsOpt.isEmpty()) return;

        UserLimit limits = limitsOpt.get();
        validateLimit(userId, amount, WalletEntryType.DEPOSIT, limits.getDailyDepositLimit(), OffsetDateTime.now().minusDays(1), "Daily deposit limit exceeded");
        validateLimit(userId, amount, WalletEntryType.DEPOSIT, limits.getWeeklyDepositLimit(), OffsetDateTime.now().minusDays(7), "Weekly deposit limit exceeded");
        validateLimit(userId, amount, WalletEntryType.DEPOSIT, limits.getMonthlyDepositLimit(), OffsetDateTime.now().minusDays(30), "Monthly deposit limit exceeded");
    }

    @Transactional(readOnly = true)
    public void validateStake(Long userId, BigDecimal amount) {
        Optional<UserLimit> limitsOpt = userLimitRepository.findByUserId(userId);
        if (limitsOpt.isEmpty()) return;

        UserLimit limits = limitsOpt.get();
        // Stake is negative in WalletEntry (BET_RESERVE), so we use absolute values for comparison
        validateLimit(userId, amount, WalletEntryType.BET_RESERVE, limits.getDailyStakeLimit(), OffsetDateTime.now().minusDays(1), "Daily stake limit exceeded");
        validateLimit(userId, amount, WalletEntryType.BET_RESERVE, limits.getWeeklyStakeLimit(), OffsetDateTime.now().minusDays(7), "Weekly stake limit exceeded");
        validateLimit(userId, amount, WalletEntryType.BET_RESERVE, limits.getMonthlyStakeLimit(), OffsetDateTime.now().minusDays(30), "Monthly stake limit exceeded");
    }

    private void validateLimit(Long userId, BigDecimal amount, WalletEntryType type, BigDecimal limit, OffsetDateTime since, String message) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal currentSum = walletEntryRepository.sumAmountByUserIdAndTypeSince(userId, type, since);
        if (currentSum == null) currentSum = BigDecimal.ZERO;
        
        // BET_RESERVE is negative, so sum is negative. Use ABS for comparison.
        BigDecimal absoluteCurrentSum = currentSum.abs();
        
        if (absoluteCurrentSum.add(amount).compareTo(limit) > 0) {
            throw new IllegalStateException(message + ". Limit: " + limit + ", Current: " + absoluteCurrentSum + ", Request: " + amount);
        }
    }

    @Transactional
    public void applySelfExclusion(User user, OffsetDateTime endAt, String reason) {
        user.setStatus(UserStatus.SELF_EXCLUDED);
        userRepository.save(user);

        SelfExclusion exclusion = SelfExclusion.builder()
                .user(user)
                .startAt(OffsetDateTime.now())
                .endAt(endAt)
                .reason(reason)
                .build();
        selfExclusionRepository.save(exclusion);

        auditService.log("SELF_EXCLUSION", "USER", user.getId().toString(), 
                "Applied self-exclusion until " + (endAt != null ? endAt : "forever") + ". Reason: " + reason);
    }

    @Transactional
    public void updateLimits(User user, UserLimit newLimits) {
        UserLimit currentLimits = userLimitRepository.findByUserId(user.getId())
                .orElse(UserLimit.builder().user(user).build());
        
        // In a real system, we'd check for limit increases and apply a 24h delay.
        // For this iteration, we apply immediately but log extensively.
        
        currentLimits.setDailyDepositLimit(newLimits.getDailyDepositLimit());
        currentLimits.setWeeklyDepositLimit(newLimits.getWeeklyDepositLimit());
        currentLimits.setMonthlyDepositLimit(newLimits.getMonthlyDepositLimit());
        currentLimits.setDailyStakeLimit(newLimits.getDailyStakeLimit());
        currentLimits.setWeeklyStakeLimit(newLimits.getWeeklyStakeLimit());
        currentLimits.setMonthlyStakeLimit(newLimits.getMonthlyStakeLimit());
        
        userLimitRepository.save(currentLimits);
        
        auditService.log("UPDATE_LIMITS", "USER", user.getId().toString(), "Updated responsible gaming limits");
    }
}
