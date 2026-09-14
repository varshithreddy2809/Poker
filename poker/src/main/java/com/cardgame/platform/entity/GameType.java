package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "game_types")
@Getter
@Setter
@NoArgsConstructor
public class GameType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short gameTypeId;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "min_players", nullable = false)
    private Byte minPlayers;

    @Column(name = "max_players", nullable = false)
    private Byte maxPlayers;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_config", columnDefinition = "json")
    private String rulesConfig;
}
