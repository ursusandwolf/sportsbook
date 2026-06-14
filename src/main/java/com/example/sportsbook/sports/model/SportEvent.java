package com.example.sportsbook.sports.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sport_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(nullable = false)
    private String name;

    @Column(name = "home_participant", nullable = false)
    private String homeParticipant;

    @Column(name = "away_participant", nullable = false)
    private String awayParticipant;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Version
    @Column(nullable = false)
    private Long version;
}
