package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_cards")
@Getter
@Setter
@NoArgsConstructor
public class PlayerCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerCardId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private GameRound gameRound;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_player_id", nullable = false)
    private GamePlayer gamePlayer;

    @Column(name = "card_rank", nullable = false, length = 5)
    private String cardRank;

    @Column(name = "card_suit", nullable = false, length = 20)
    private String cardSuit;

    @Column(name = "deck_number", nullable = false)
    private Byte deckNumber = 1;

    @Column(name = "card_position", nullable = false)
    private Short cardPosition;

    @Column(name = "dealt_at", insertable = false, updatable = false)
    private LocalDateTime dealtAt;
}
