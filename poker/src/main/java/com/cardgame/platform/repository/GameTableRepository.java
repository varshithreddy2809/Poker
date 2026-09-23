package com.cardgame.platform.repository;

import com.cardgame.platform.entity.GameTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameTableRepository extends JpaRepository<GameTable, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select table from GameTable table where table.tableId = :tableId")
    Optional<GameTable> findByTableIdForUpdate(@Param("tableId") Long tableId);
}
