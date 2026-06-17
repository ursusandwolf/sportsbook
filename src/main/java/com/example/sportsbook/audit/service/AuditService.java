package com.example.sportsbook.audit.service;

import com.example.sportsbook.audit.model.AuditEvent;
import com.example.sportsbook.audit.repository.AuditEventRepository;
import com.example.sportsbook.security.SecurityUser;
import com.example.sportsbook.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String subjectType, String subjectId, String details) {
        String actorId = SecurityUtils.getCurrentUser()
                .map(u -> u.getId().toString())
                .orElse("SYSTEM");
        
        AuditEvent event = AuditEvent.builder()
                .actorType("USER")
                .actorId(actorId)
                .action(action)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .details(details)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        auditEventRepository.save(event);
    }
}
