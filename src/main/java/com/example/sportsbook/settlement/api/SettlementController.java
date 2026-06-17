package com.example.sportsbook.settlement.api;

import com.example.sportsbook.settlement.api.dto.SettlementRequest;
import com.example.sportsbook.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bets")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/{betId}/settle")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<Void> settle(@PathVariable Long betId, @Valid @RequestBody SettlementRequest request) {
        settlementService.settle(betId, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
