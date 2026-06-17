package com.example.sportsbook.common.repository;

import com.example.sportsbook.common.model.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatus(OutboxEvent.OutboxStatus status, Pageable pageable);
}
