package com.example.sportsbook.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "withdrawal_destinations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalDestination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String fingerprint;

    @Column(nullable = false)
    private String type; // e.g., BANK_ACCOUNT, CRYPTO_WALLET

    @CreationTimestamp
    private Instant createdAt;
}
