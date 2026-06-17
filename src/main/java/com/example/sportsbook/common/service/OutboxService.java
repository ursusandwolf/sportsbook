package com.example.sportsbook.common.service;

import com.example.sportsbook.common.model.OutboxEvent;
import com.example.sportsbook.common.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void saveEvent(String eventType, String payload) {
        OutboxEvent event = OutboxEvent.builder()
                .eventType(eventType)
                .payload(payload)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        outboxEventRepository.save(event);
    }
}
