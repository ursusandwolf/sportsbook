package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudLink;
import com.example.sportsbook.fraud.model.FraudLinkType;
import com.example.sportsbook.fraud.repository.FraudLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudLinkageServiceTests {

    @Mock
    private FraudLinkRepository fraudLinkRepository;

    @InjectMocks
    private FraudLinkageService fraudLinkageService;

    @Test
    void shouldCreateNewLink() {
        Long userA = 1L;
        Long userB = 2L;
        FraudLinkType type = FraudLinkType.DEVICE;

        when(fraudLinkRepository.findByLeftUserIdAndRightUserIdAndLinkType(1L, 2L, type))
                .thenReturn(Optional.empty());
        when(fraudLinkRepository.save(any(FraudLink.class))).thenAnswer(i -> i.getArguments()[0]);

        FraudLink link = fraudLinkageService.createOrUpdateLink(userA, userB, type, 10);

        assertEquals(1L, link.getLeftUserId());
        assertEquals(2L, link.getRightUserId());
        assertEquals(10, link.getStrength());
    }

    @Test
    void shouldUpdateExistingLink() {
        Long userA = 1L;
        Long userB = 2L;
        FraudLinkType type = FraudLinkType.DEVICE;
        FraudLink existingLink = FraudLink.builder()
                .leftUserId(1L)
                .rightUserId(2L)
                .linkType(type)
                .strength(10)
                .evidenceCount(1)
                .build();

        when(fraudLinkRepository.findByLeftUserIdAndRightUserIdAndLinkType(1L, 2L, type))
                .thenReturn(Optional.of(existingLink));
        when(fraudLinkRepository.save(any(FraudLink.class))).thenAnswer(i -> i.getArguments()[0]);

        FraudLink link = fraudLinkageService.createOrUpdateLink(userA, userB, type, 5);

        assertEquals(15, link.getStrength());
        assertEquals(2, link.getEvidenceCount());
    }
}
