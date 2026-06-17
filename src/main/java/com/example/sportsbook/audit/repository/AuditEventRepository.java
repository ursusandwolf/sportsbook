package com.example.sportsbook.audit.repository;

import com.example.sportsbook.audit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
