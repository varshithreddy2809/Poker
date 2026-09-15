package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "round_players")
@Getter
@Setter
@NoArgsConstructor
public class RoundPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roundPlayerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private GameRound gameRound;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_player_id", nullable = false)
    private GamePlayer gamePlayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_status", nullable = false, length = 20)
    private VisibilityStatus visibilityStatus = VisibilityStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_player_status", nullable = false, length = 20)
    private RoundPlayerStatus roundPlayerStatus = RoundPlayerStatus.ACTIVE;

    @Column(name = "total_contribution", nullable = false)
    private Long totalContribution = 0L;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @Column(name = "dropped_at")
    private LocalDateTime droppedAt;
}
