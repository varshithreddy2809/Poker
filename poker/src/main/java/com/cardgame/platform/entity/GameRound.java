package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_rounds")
@Getter
@Setter
@NoArgsConstructor
public class GameRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roundId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private GameTable gameTable;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_status", nullable = false, length = 20)
    private GameRoundStatus roundStatus = GameRoundStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GamePhase phase = GamePhase.CHOOSING_VISIBILITY;

    @Column(name = "current_bet", nullable = false)
    private Long currentBet = 0L;

    @Column(nullable = false)
    private Long pot = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_turn_game_player_id")
    private GamePlayer currentTurnGamePlayer;

    @Column(name = "visibility_deadline")
    private LocalDateTime visibilityDeadline;

    @Column(name = "turn_deadline")
    private LocalDateTime turnDeadline;

    @Column(name = "forced_same_turns_remaining", nullable = false)
    private Byte forcedSameTurnsRemaining = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_player_id")
    private Player winnerPlayer;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}
