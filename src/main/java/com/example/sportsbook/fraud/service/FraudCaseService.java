package com.example.sportsbook.fraud.service;

import com.example.sportsbook.audit.service.AuditService;
import com.example.sportsbook.fraud.model.FraudCase;
import com.example.sportsbook.fraud.model.FraudCaseStatus;
import com.example.sportsbook.fraud.repository.FraudCaseRepository;
import com.example.sportsbook.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FraudCaseService {

    private final FraudCaseRepository fraudCaseRepository;
    private final AuditService auditService;

    @Transactional
    public FraudCase createCase(User user, Long assessmentId, Integer priority, String reason) {
        FraudCase fraudCase = FraudCase.builder()
                .user(user)
                .assessmentId(assessmentId)
                .status(FraudCaseStatus.OPEN)
                .priority(priority)
                .reason(reason)
                .createdAt(OffsetDateTime.now())
                .version(0L)
                .build();
        
        fraudCase = fraudCaseRepository.save(fraudCase);
        
        auditService.log("FRAUD_CASE_CREATED", "SYSTEM", user.getId().toString(),
                "New fraud case " + fraudCase.getId() + " created for user " + user.getId());
        
        return fraudCase;
    }

    @Transactional
    public void updateStatus(Long caseId, FraudCaseStatus status, Long analystId, String comment) {
        FraudCase fraudCase = fraudCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        
        fraudCase.setStatus(status);
        fraudCase.setAssignedAnalystId(analystId);
        fraudCase.setUpdatedAt(OffsetDateTime.now());
        
        if (status == FraudCaseStatus.CONFIRMED_FRAUD || status == FraudCaseStatus.FALSE_POSITIVE || status == FraudCaseStatus.CLOSED) {
            fraudCase.setResolvedAt(OffsetDateTime.now());
        }
        
        fraudCaseRepository.save(fraudCase);
        
        auditService.log("FRAUD_CASE_UPDATED", "RISK_ANALYST", analystId.toString(),
                "Case " + caseId + " status updated to " + status + ". Comment: " + comment);
    }
}
