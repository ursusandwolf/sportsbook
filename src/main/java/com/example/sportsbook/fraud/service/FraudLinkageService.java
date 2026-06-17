package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudLink;
import com.example.sportsbook.fraud.model.FraudLinkType;
import com.example.sportsbook.fraud.repository.FraudLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudLinkageService {

    private final FraudLinkRepository fraudLinkRepository;

    public FraudLink createOrUpdateLink(Long userA, Long userB, FraudLinkType type, int strength) {
        Long left = Math.min(userA, userB);
        Long right = Math.max(userA, userB);

        return fraudLinkRepository.findByLeftUserIdAndRightUserIdAndLinkType(left, right, type)
                .map(existingLink -> {
                    existingLink.setStrength(existingLink.getStrength() + strength);
                    existingLink.setEvidenceCount(existingLink.getEvidenceCount() + 1);
                    existingLink.setLastDetectedAt(LocalDateTime.now());
                    return fraudLinkRepository.save(existingLink);
                })
                .orElseGet(() -> {
                    FraudLink newLink = FraudLink.builder()
                            .leftUserId(left)
                            .rightUserId(right)
                            .linkType(type)
                            .strength(strength)
                            .evidenceCount(1)
                            .firstDetectedAt(LocalDateTime.now())
                            .lastDetectedAt(LocalDateTime.now())
                            .status("ACTIVE")
                            .build();
                    return fraudLinkRepository.save(newLink);
                });
    }
}
