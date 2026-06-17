package com.example.sportsbook.fraud.api;

import com.example.sportsbook.fraud.model.FraudCaseStatus;
import com.example.sportsbook.fraud.service.FraudCaseService;
import com.example.sportsbook.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fraud/cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_RISK_ANALYST')")
public class FraudCaseController {

    private final FraudCaseService fraudCaseService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestParam FraudCaseStatus status,
                                             @RequestParam(required = false) String comment,
                                             @AuthenticationPrincipal SecurityUser analyst) {
        fraudCaseService.updateStatus(id, status, analyst.getUser().getId(), comment);
        return ResponseEntity.noContent().build();
    }
}
