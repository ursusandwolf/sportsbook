package com.example.sportsbook.bonus.api;

import com.example.sportsbook.bonus.service.BonusService;
import com.example.sportsbook.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/bonuses")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;

    @GetMapping("/me/balance")
    public ResponseEntity<BigDecimal> getMyBonusBalance() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(bonusService.getActiveBonusBalance(userId));
    }
}
