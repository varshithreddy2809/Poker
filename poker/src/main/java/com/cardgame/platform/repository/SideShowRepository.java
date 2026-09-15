package com.cardgame.platform.repository;

import com.cardgame.platform.entity.SideShow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SideShowRepository extends JpaRepository<SideShow, Long> {
    Optional<SideShow> findBySideShowIdAndStatus(Long sideShowId, String status);
    Optional<SideShow> findTopByGameRound_RoundIdAndStatusOrderByCreatedAtDesc(Long roundId, String status);
}
