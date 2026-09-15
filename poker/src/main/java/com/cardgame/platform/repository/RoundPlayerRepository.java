package com.cardgame.platform.repository;

import com.cardgame.platform.entity.RoundPlayer;
import com.cardgame.platform.entity.RoundPlayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoundPlayerRepository extends JpaRepository<RoundPlayer, Long> {
    List<RoundPlayer> findByGameRound_RoundIdOrderByGamePlayer_SeatNumber(Long roundId);
    List<RoundPlayer> findByGameRound_RoundIdAndRoundPlayerStatusOrderByGamePlayer_SeatNumber(Long roundId, RoundPlayerStatus status);
    Optional<RoundPlayer> findByGameRound_RoundIdAndGamePlayer_Player_PlayerId(Long roundId, Long playerId);
    Optional<RoundPlayer> findByGameRound_RoundIdAndGamePlayer_GamePlayerId(Long roundId, Long gamePlayerId);
}
