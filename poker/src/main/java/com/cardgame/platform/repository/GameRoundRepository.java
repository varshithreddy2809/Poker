package com.cardgame.platform.repository;

import com.cardgame.platform.entity.GameRound;
import com.cardgame.platform.entity.GameRoundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select gameRound from GameRound gameRound where gameRound.roundId = :roundId")
    Optional<GameRound> findByRoundIdForUpdate(@Param("roundId") Long roundId);

    boolean existsByGameTable_TableIdAndRoundStatus(Long tableId, GameRoundStatus status);
    Optional<GameRound> findByGameTable_TableIdAndRoundStatus(Long tableId, GameRoundStatus status);
    Optional<GameRound> findTopByGameTable_TableIdOrderByRoundNumberDesc(Long tableId);
    List<GameRound> findByRoundStatus(GameRoundStatus status);
}
