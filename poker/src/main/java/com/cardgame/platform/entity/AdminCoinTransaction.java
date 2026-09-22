package com.cardgame.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_coin_transactions", uniqueConstraints =
        @UniqueConstraint(name = "uk_admin_coin_operation_target", columnNames = {"operation_id", "target_player_id"}))
@Getter @Setter @NoArgsConstructor
public class AdminCoinTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "admin_id", nullable = false)
    private Player admin;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "target_player_id", nullable = false)
    private Player targetPlayer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "table_id")
    private GameTable gameTable;
    @Column(name = "operation_id", nullable = false, length = 80)
    private String operationId;
    @Column(name = "operation_type", nullable = false, length = 30)
    private String operationType;
    @Column(nullable = false)
    private Long amount;
    @Column(nullable = false, length = 250)
    private String reason;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void initializeTimestamp() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
