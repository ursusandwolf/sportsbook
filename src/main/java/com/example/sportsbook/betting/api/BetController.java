package com.example.sportsbook.betting.api;

import com.example.sportsbook.betting.api.dto.BetResponse;
import com.example.sportsbook.betting.api.dto.PlaceBetRequest;
import com.example.sportsbook.betting.service.BetService;
import com.example.sportsbook.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    public BetResponse placeBet(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody PlaceBetRequest request) {
        return betService.placeBet(securityUser.getUser(), request);
    }
}
