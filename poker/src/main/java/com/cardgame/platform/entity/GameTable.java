package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_tables")
@Getter
@Setter
@NoArgsConstructor
public class GameTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tableId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_type_id", nullable = false)
    private GameType gameType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_player_id", nullable = false)
    private Player hostPlayer;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "entry_bet", nullable = false)
    private Long entryBet;

    @Column(name = "max_players", nullable = false)
    private Byte maxPlayers;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_status", nullable = false, length = 20)
    private GameTableStatus tableStatus = GameTableStatus.OPEN;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
