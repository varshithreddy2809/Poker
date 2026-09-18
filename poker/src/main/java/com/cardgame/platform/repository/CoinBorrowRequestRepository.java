package com.cardgame.platform.repository;
import com.cardgame.platform.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface CoinBorrowRequestRepository extends JpaRepository<CoinBorrowRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<CoinBorrowRequest> findWithLockByBorrowRequestId(Long borrowRequestId);
    List<CoinBorrowRequest> findByLender_PlayerIdAndRequestStatusOrderByCreatedAtAsc(Long lenderId, BorrowRequestStatus status);
    List<CoinBorrowRequest> findByBorrower_PlayerIdAndRequestStatusOrderByCreatedAtAsc(Long borrowerId, BorrowRequestStatus status);
}
