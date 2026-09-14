package com.cardgame.platform.repository;

import com.cardgame.platform.entity.GameTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameTableRepository extends JpaRepository<GameTable, Long> {
}
