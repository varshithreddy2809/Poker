package com.cardgame.platform.repository;

import com.cardgame.platform.entity.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerCardRepository extends JpaRepository<PlayerCard, Long> {
    List<PlayerCard> findByGameRound_RoundIdAndGamePlayer_GamePlayerIdOrderByCardPosition(Long roundId, Long gamePlayerId);
    List<PlayerCard> findByGameRound_RoundIdOrderByGamePlayer_SeatNumberAscCardPositionAsc(Long roundId);
}
