package com.cardgame.platform.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name = "player_debts") @Getter @Setter @NoArgsConstructor
public class PlayerDebt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long debtId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "borrower_player_id") private Player borrower;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lender_player_id") private Player lender;
    @Column(name = "original_amount") private Long originalAmount;
    @Column(name = "remaining_amount") private Long remainingAmount;
    @Column(name = "debt_status") private String debtStatus;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "settled_at") private LocalDateTime settledAt;
}
