package com.cardgame.platform.repository;
import com.cardgame.platform.entity.PlayerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
public interface PlayerDebtRepository extends JpaRepository<PlayerDebt, Long> {
    List<PlayerDebt> findByBorrower_PlayerIdAndDebtStatusNotOrderByCreatedAtAsc(Long borrowerId, String debtStatus);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<PlayerDebt> findWithLockByBorrower_PlayerIdAndDebtStatusNotOrderByCreatedAtAsc(Long borrowerId, String debtStatus);
}
