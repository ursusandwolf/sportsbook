package com.example.sportsbook.fraud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fingerprintHash;

    private String userAgent;
    private String ipAddress;
    private String language;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;
}
