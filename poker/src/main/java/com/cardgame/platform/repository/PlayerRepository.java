package com.cardgame.platform.repository;

import com.cardgame.platform.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Player> findByUsernameIgnoreCase(String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Player> findWithLockByPlayerId(Long playerId);
}
