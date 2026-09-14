package com.cardgame.platform.repository;

import com.cardgame.platform.entity.GameRound;
import com.cardgame.platform.entity.GameRoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
    boolean existsByGameTable_TableIdAndRoundStatus(Long tableId, GameRoundStatus status);
    Optional<GameRound> findTopByGameTable_TableIdOrderByRoundNumberDesc(Long tableId);
}
