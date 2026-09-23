package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_players")
@Getter
@Setter
@NoArgsConstructor
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gamePlayerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private GameTable gameTable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "seat_number", nullable = false)
    private Byte seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_status", nullable = false, length = 20)
    private GamePlayerStatus playerStatus = GamePlayerStatus.JOINED;

    /** Server-owned waiting-room roster for the next deal. */
    @Column(name = "next_round_ready", nullable = false)
    private Boolean nextRoundReady = false;

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;
}
