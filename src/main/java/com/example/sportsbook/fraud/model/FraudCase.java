package com.example.sportsbook.fraud.model;

import com.example.sportsbook.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fraud_cases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudCaseStatus status;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "assigned_analyst_id")
    private Long assignedAnalystId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private OffsetDateTime resolvedAt;

    @Version
    private Long version;
}
