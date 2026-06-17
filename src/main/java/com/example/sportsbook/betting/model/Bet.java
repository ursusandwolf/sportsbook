package com.example.sportsbook.betting.model;

import com.example.sportsbook.market.model.Selection;
import com.example.sportsbook.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selection_id", nullable = false)
    private Selection selection;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal stake;

    @Column(name = "odds_at_placement", nullable = false, precision = 19, scale = 4)
    private BigDecimal oddsAtPlacement;

    @Column(name = "potential_payout", nullable = false, precision = 19, scale = 2)
    private BigDecimal potentialPayout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status;

    @Column(name = "placed_at", nullable = false)
    private OffsetDateTime placedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
