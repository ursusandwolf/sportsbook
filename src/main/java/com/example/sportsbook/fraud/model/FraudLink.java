package com.example.sportsbook.fraud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_links")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leftUserId;

    @Column(nullable = false)
    private Long rightUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudLinkType linkType;

    @Column(nullable = false)
    private int strength;

    private int evidenceCount;

    @Column(nullable = false)
    private LocalDateTime firstDetectedAt;

    @Column(nullable = false)
    private LocalDateTime lastDetectedAt;

    @Column(nullable = false)
    private String status; // e.g., ACTIVE, DEPRECATED
}
