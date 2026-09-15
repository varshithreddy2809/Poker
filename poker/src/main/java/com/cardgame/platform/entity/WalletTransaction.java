package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter @Setter @NoArgsConstructor
public class WalletTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletTransactionId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "player_id")
    private Player player;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "round_id")
    private GameRound gameRound;
    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;
    @Column(nullable = false)
    private Long amount;
    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
