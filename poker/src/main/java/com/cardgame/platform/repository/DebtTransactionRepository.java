package com.cardgame.platform.repository;
import com.cardgame.platform.entity.DebtTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DebtTransactionRepository extends JpaRepository<DebtTransaction, Long> { }
