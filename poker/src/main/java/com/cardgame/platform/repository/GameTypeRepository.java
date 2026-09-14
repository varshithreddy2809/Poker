package com.cardgame.platform.repository;

import com.cardgame.platform.entity.GameType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameTypeRepository extends JpaRepository<GameType, Short> {
    Optional<GameType> findByCodeAndActiveTrue(String code);
}
