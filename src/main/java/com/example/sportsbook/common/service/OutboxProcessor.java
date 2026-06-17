package com.example.sportsbook.common.service;

import com.example.sportsbook.common.model.OutboxEvent;
import com.example.sportsbook.common.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final Counter processedEventsCounter;
    private final Timer processingTimer;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository, MeterRegistry meterRegistry) {
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventsCounter = meterRegistry.counter("outbox.events.processed");
        this.processingTimer = meterRegistry.timer("outbox.events.processing.time");
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingEvents() {
        processingTimer.record(() -> {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findAll().stream()
                    .filter(event -> event.getStatus() == OutboxEvent.OutboxStatus.PENDING)
                    .toList();

            for (OutboxEvent event : pendingEvents) {
                log.info("Processing event: {}", event.getEventType());
                // In real system, this would publish to Kafka or call another service
                event.setStatus(OutboxEvent.OutboxStatus.PROCESSED);
                event.setProcessedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
                processedEventsCounter.increment();
            }
        });
    }
}
