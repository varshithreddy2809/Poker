package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "side_shows")
@Getter @Setter @NoArgsConstructor
public class SideShow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sideShowId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "round_id")
    private GameRound gameRound;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requester_game_player_id")
    private GamePlayer requesterGamePlayer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requested_game_player_id")
    private GamePlayer requestedGamePlayer;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "responded_at") private LocalDateTime respondedAt;

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
