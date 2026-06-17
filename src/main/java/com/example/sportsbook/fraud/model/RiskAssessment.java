package com.example.sportsbook.fraud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long subjectUserId;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskDecision decision;

    @Column(nullable = false)
    private String policyVersion;

    @Column(nullable = false)
    private LocalDateTime assessedAt;

    // In a real project, consider a more structured way to store triggered rules and their weights
    @ElementCollection
    @CollectionTable(name = "risk_assessment_rules", joinColumns = @JoinColumn(name = "assessment_id"))
    @MapKeyColumn(name = "rule_code")
    @Column(name = "weight")
    private Map<String, Integer> triggeredRules;
}
