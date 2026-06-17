package com.example.sportsbook.audit.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_type", nullable = false)
    private String actorType;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "subject_type", nullable = false)
    private String subjectType;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(length = 4000)
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;
}
