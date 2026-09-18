package com.cardgame.platform.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name = "debt_transactions") @Getter @Setter @NoArgsConstructor
public class DebtTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long debtTransactionId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "debt_id") private PlayerDebt debt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "round_id") private GameRound gameRound;
    private Long amount;
    @Column(name = "transaction_type") private String transactionType;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
}
