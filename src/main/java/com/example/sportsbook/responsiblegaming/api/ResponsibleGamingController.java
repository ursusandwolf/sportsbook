package com.example.sportsbook.responsiblegaming.api;

import com.example.sportsbook.responsiblegaming.api.dto.SelfExclusionRequest;
import com.example.sportsbook.responsiblegaming.api.dto.UserLimitRequest;
import com.example.sportsbook.responsiblegaming.model.UserLimit;
import com.example.sportsbook.responsiblegaming.service.ResponsibleGamingService;
import com.example.sportsbook.security.SecurityUtils;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/responsible-gaming")
@RequiredArgsConstructor
public class ResponsibleGamingController {

    private final ResponsibleGamingService responsibleGamingService;
    private final UserService userService;

    @PostMapping("/limits")
    public ResponseEntity<Void> updateLimits(@RequestBody UserLimitRequest request) {
        User user = getCurrentUser();
        UserLimit newLimits = UserLimit.builder()
                .dailyDepositLimit(request.dailyDepositLimit())
                .weeklyDepositLimit(request.weeklyDepositLimit())
                .monthlyDepositLimit(request.monthlyDepositLimit())
                .dailyStakeLimit(request.dailyStakeLimit())
                .weeklyStakeLimit(request.weeklyStakeLimit())
                .monthlyStakeLimit(request.monthlyStakeLimit())
                .build();
        
        responsibleGamingService.updateLimits(user, newLimits);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/self-exclude")
    public ResponseEntity<Void> selfExclude(@RequestBody SelfExclusionRequest request) {
        User user = getCurrentUser();
        responsibleGamingService.applySelfExclusion(user, request.endAt(), request.reason());
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
