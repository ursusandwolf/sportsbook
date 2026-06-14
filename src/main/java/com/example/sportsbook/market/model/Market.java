package com.example.sportsbook.market.model;

import com.example.sportsbook.sports.model.SportEvent;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "markets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private SportEvent event;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_type", nullable = false)
    private MarketType marketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketStatus status;

    @Version
    @Column(nullable = false)
    private Long version;
}
