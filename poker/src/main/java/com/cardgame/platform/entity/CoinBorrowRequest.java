package com.cardgame.platform.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name = "coin_borrow_requests") @Getter @Setter @NoArgsConstructor
public class CoinBorrowRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long borrowRequestId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "round_id") private GameRound gameRound;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "borrower_player_id") private Player borrower;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lender_player_id") private Player lender;
    private Long amount;
    @Enumerated(EnumType.STRING) @Column(name = "request_status") private BorrowRequestStatus requestStatus;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "responded_at") private LocalDateTime respondedAt;
}
