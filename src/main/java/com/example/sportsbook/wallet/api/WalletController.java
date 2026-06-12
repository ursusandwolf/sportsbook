package com.example.sportsbook.wallet.api;

import com.example.sportsbook.security.SecurityUser;
import com.example.sportsbook.wallet.api.dto.DepositRequest;
import com.example.sportsbook.wallet.api.dto.WalletResponse;
import com.example.sportsbook.wallet.api.dto.WithdrawalRequest;
import com.example.sportsbook.wallet.exception.WalletNotFoundException;
import com.example.sportsbook.wallet.model.Wallet;
import com.example.sportsbook.wallet.repository.WalletRepository;
import com.example.sportsbook.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletRepository walletRepository;

    @GetMapping("/me")
    public WalletResponse getMyWallet(@AuthenticationPrincipal SecurityUser securityUser) {
        Wallet wallet = walletRepository.findByUserId(securityUser.getUser().getId())
                .orElseThrow(() -> new WalletNotFoundException(securityUser.getUser().getId()));
        
        return new WalletResponse(
                wallet.getAvailableBalance(),
                wallet.getReservedBalance(),
                wallet.getCurrency()
        );
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLAYER')")
    public void deposit(@AuthenticationPrincipal SecurityUser securityUser, 
                        @Valid @RequestBody DepositRequest request) {
        walletService.deposit(securityUser.getUser().getId(), request.amount(), request.idempotencyKey());
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLAYER')")
    public void withdraw(@AuthenticationPrincipal SecurityUser securityUser, 
                         @Valid @RequestBody WithdrawalRequest request) {
        walletService.withdraw(securityUser.getUser().getId(), request.amount(), request.idempotencyKey());
    }
}
