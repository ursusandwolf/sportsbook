package com.example.sportsbook.wallet.service;

import com.example.sportsbook.responsiblegaming.service.ResponsibleGamingService;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.model.UserStatus;
import com.example.sportsbook.wallet.exception.InsufficientFundsException;
import com.example.sportsbook.wallet.exception.WalletNotFoundException;
import com.example.sportsbook.wallet.model.Wallet;
import com.example.sportsbook.wallet.model.WalletEntry;
import com.example.sportsbook.wallet.model.WalletEntryType;
import com.example.sportsbook.wallet.repository.WalletEntryRepository;
import com.example.sportsbook.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final WalletRepository walletRepository;
    private final WalletEntryRepository walletEntryRepository;
    private final ResponsibleGamingService responsibleGamingService;

    @Transactional
    public Wallet createWallet(User user) {
        return walletRepository.save(Wallet.builder()
                .user(user)
                .availableBalance(zero())
                .reservedBalance(zero())
                .bonusBalance(zero())
                .currency("USD")
                .build());
    }

    @Transactional
    public void deposit(Long userId, BigDecimal amount, String idempotencyKey) {
        responsibleGamingService.validateDeposit(userId, amount);
        applyLedgerEntry(userId, amount, idempotencyKey, WalletEntryType.DEPOSIT, null, null, (wallet, scaledAmount) -> {
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(scaledAmount));
            return scaledAmount;
        });
    }

    @Transactional
    public void reserveFunds(Long userId, BigDecimal amount, String referenceType, String referenceId, String idempotencyKey) {
        reserve(userId, amount, WalletEntryType.BET_RESERVE, referenceType, referenceId, idempotencyKey);
    }

    @Transactional
    public void reserveWithdrawal(Long userId, BigDecimal amount, String withdrawalId, String idempotencyKey) {
        reserve(userId, amount, WalletEntryType.WITHDRAWAL_RESERVE, "WITHDRAWAL", withdrawalId, idempotencyKey);
    }

    @Transactional
    public void settleWin(Long userId, BigDecimal stake, BigDecimal payout, String referenceType, String referenceId,
                          String idempotencyKey) {
        BigDecimal scaledStake = scale(stake);
        applyLedgerEntry(userId, payout, idempotencyKey, WalletEntryType.WIN_PAYOUT, referenceType, referenceId,
                (wallet, scaledPayout) -> {
                    wallet.setAvailableBalance(wallet.getAvailableBalance().add(scaledPayout));
                    wallet.setReservedBalance(wallet.getReservedBalance().subtract(scaledStake));
                    return scaledPayout;
                });
    }

    @Transactional
    public void settleLoss(Long userId, BigDecimal stake, String referenceType, String referenceId, String idempotencyKey) {
        BigDecimal scaledStake = scale(stake);
        applyLedgerEntry(userId, BigDecimal.ZERO, idempotencyKey, WalletEntryType.BET_LOSS, referenceType, referenceId,
                (wallet, ignored) -> {
                    wallet.setReservedBalance(wallet.getReservedBalance().subtract(scaledStake));
                    return zero();
                });
    }

    @Transactional
    public void settleRefund(Long userId, BigDecimal stake, String referenceType, String referenceId, String idempotencyKey) {
        applyLedgerEntry(userId, stake, idempotencyKey, WalletEntryType.REFUND, referenceType, referenceId,
                (wallet, scaledStake) -> {
                    wallet.setAvailableBalance(wallet.getAvailableBalance().add(scaledStake));
                    wallet.setReservedBalance(wallet.getReservedBalance().subtract(scaledStake));
                    return scaledStake;
                });
    }

    private void reserve(Long userId, BigDecimal amount, WalletEntryType type, String referenceType, String referenceId,
                         String idempotencyKey) {
        applyLedgerEntry(userId, amount, idempotencyKey, type, referenceType, referenceId, (wallet, scaledAmount) -> {
            if (wallet.getAvailableBalance().compareTo(scaledAmount) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }
            wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(scaledAmount));
            wallet.setReservedBalance(wallet.getReservedBalance().add(scaledAmount));
            return scaledAmount.negate();
        });
    }

    private void applyLedgerEntry(Long userId, BigDecimal amount, String idempotencyKey, WalletEntryType type,
                                  String referenceType, String referenceId, BalanceMutation mutation) {
        BigDecimal scaledAmount = scale(amount);
        if (scaledAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (idempotencyKey != null && walletEntryRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return;
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        assertAllowedDuringSelfExclusion(wallet, type);

        BigDecimal ledgerAmount = mutation.apply(wallet, scaledAmount);
        walletRepository.save(wallet);

        walletEntryRepository.save(WalletEntry.builder()
                .wallet(wallet)
                .type(type)
                .amount(ledgerAmount)
                .balanceAfter(wallet.getAvailableBalance())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .idempotencyKey(idempotencyKey)
                .build());
    }

    private void assertAllowedDuringSelfExclusion(Wallet wallet, WalletEntryType type) {
        if (wallet.getUser().getStatus() != UserStatus.SELF_EXCLUDED) {
            return;
        }
        if (type == WalletEntryType.DEPOSIT || type == WalletEntryType.BET_RESERVE) {
            throw new IllegalStateException("Action not allowed during self-exclusion: " + type);
        }
    }

    private BigDecimal scale(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
    }

    @FunctionalInterface
    private interface BalanceMutation {
        BigDecimal apply(Wallet wallet, BigDecimal scaledAmount);
    }
}
