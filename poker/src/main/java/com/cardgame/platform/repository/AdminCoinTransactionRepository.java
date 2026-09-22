package com.cardgame.platform.repository;

import com.cardgame.platform.entity.AdminCoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminCoinTransactionRepository extends JpaRepository<AdminCoinTransaction, Long> {
    Optional<AdminCoinTransaction> findByOperationIdAndTargetPlayer_PlayerId(String operationId, Long playerId);
    List<AdminCoinTransaction> findAllByOrderByCreatedAtDesc();
}
