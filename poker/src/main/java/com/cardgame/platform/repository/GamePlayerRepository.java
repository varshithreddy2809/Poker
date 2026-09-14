package com.cardgame.platform.repository;

import com.cardgame.platform.entity.GamePlayer;
import com.cardgame.platform.entity.GamePlayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    List<GamePlayer> findByGameTable_TableIdAndPlayerStatusOrderBySeatNumber(Long tableId, GamePlayerStatus status);
    List<GamePlayer> findByGameTable_TableIdOrderBySeatNumber(Long tableId);
    Optional<GamePlayer> findByGameTable_TableIdAndPlayer_PlayerId(Long tableId, Long playerId);
    boolean existsByGameTable_TableIdAndPlayer_PlayerId(Long tableId, Long playerId);
}
