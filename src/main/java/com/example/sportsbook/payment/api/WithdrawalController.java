package com.example.sportsbook.payment.api;

import com.example.sportsbook.payment.model.Withdrawal;
import com.example.sportsbook.payment.service.WithdrawalService;
import com.example.sportsbook.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/withdrawals")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public ResponseEntity<Withdrawal> requestWithdrawal(@AuthenticationPrincipal SecurityUser user,
                                                        @RequestParam BigDecimal amount,
                                                        @RequestParam String destinationFingerprint) {
        return ResponseEntity.ok(withdrawalService.requestWithdrawal(user.getUser(), amount, destinationFingerprint));
    }
}
