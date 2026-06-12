package com.example.sportsbook.wallet.service;

import com.example.sportsbook.user.model.User;
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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final WalletRepository walletRepository;
    private final WalletEntryRepository walletEntryRepository;

    @Transactional
    public Wallet createWallet(User user) {
        return walletRepository.save(Wallet.builder()
                .user(user)
                .availableBalance(zero())
                .reservedBalance(zero())
                .currency("USD")
                .build());
    }

    @Transactional
    public void deposit(Long userId, BigDecimal amount, String key) {
        execute(userId, amount, key, WalletEntryType.DEPOSIT, null, null, (w, a) -> {
            w.setAvailableBalance(w.getAvailableBalance().add(a));
            return a; // Ledger amount: positive
        });
    }

    @Transactional
    public void withdraw(Long userId, BigDecimal amount, String key) {
        execute(userId, amount, key, WalletEntryType.WITHDRAWAL, null, null, (w, a) -> {
            if (w.getAvailableBalance().compareTo(a) < 0) throw new InsufficientFundsException("Insufficient funds");
            w.setAvailableBalance(w.getAvailableBalance().subtract(a));
            return a.negate(); // Ledger amount: negative
        });
    }

    @Transactional
    public void reserveFunds(Long userId, BigDecimal amount, String refT, String refId, String key) {
        execute(userId, amount, key, WalletEntryType.BET_RESERVE, refT, refId, (w, a) -> {
            if (w.getAvailableBalance().compareTo(a) < 0) throw new InsufficientFundsException("Insufficient funds");
            w.setAvailableBalance(w.getAvailableBalance().subtract(a));
            w.setReservedBalance(w.getReservedBalance().add(a));
            return a.negate(); // Ledger amount: negative
        });
    }

    /**
     * Универсальный метод для выполнения финансовых операций.
     * @param action принимает (кошелек, масштабированная сумма), возвращает сумму для записи в Ledger.
     */
    private void execute(Long userId, BigDecimal amount, String key, WalletEntryType type, String refT, String refId, 
                         FinancialAction action) {
        BigDecimal scaled = amount.setScale(SCALE, ROUNDING_MODE);
        if (scaled.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (key != null && walletEntryRepository.findByIdempotencyKey(key).isPresent()) return;

        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new WalletNotFoundException(userId));
        BigDecimal ledgerAmount = action.apply(wallet, scaled);
        walletRepository.save(wallet);

        walletEntryRepository.save(WalletEntry.builder()
                .wallet(wallet).type(type).amount(ledgerAmount).balanceAfter(wallet.getAvailableBalance())
                .referenceType(refT).referenceId(refId).idempotencyKey(key).build());
    }

    private BigDecimal zero() { return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE); }

    @FunctionalInterface
    private interface FinancialAction {
        BigDecimal apply(Wallet wallet, BigDecimal scaledAmount);
    }
}
