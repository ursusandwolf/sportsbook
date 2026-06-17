package com.example.sportsbook.fraud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_signals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long subjectUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudSignalType type;

    @Column(nullable = false)
    private int severity;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private String ruleCode;

    private String evidenceReference;

    @Column(nullable = false)
    private LocalDateTime detectedAt;
}
