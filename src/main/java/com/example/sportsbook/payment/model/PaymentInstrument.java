package com.example.sportsbook.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "payment_instruments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInstrument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String fingerprint;

    @Column(nullable = false)
    private String providerToken;

    @Column(nullable = false)
    private String type; // e.g., CARD, E_WALLET

    @CreationTimestamp
    private Instant createdAt;
}
