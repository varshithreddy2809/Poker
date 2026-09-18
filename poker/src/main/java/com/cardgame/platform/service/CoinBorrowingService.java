package com.cardgame.platform.service;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.exception.ResourceNotFoundException;
import com.cardgame.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CoinBorrowingService {
    private final CoinBorrowRequestRepository requestRepository;
    private final PlayerDebtRepository debtRepository;
    private final DebtTransactionRepository debtTransactionRepository;
    private final PlayerRepository playerRepository;
    private final GameRoundRepository roundRepository;
    private final RoundPlayerRepository roundPlayerRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TableRealtimePublisher tableRealtimePublisher;

    @Transactional
    public CoinBorrowRequestResponse request(Long roundId, Long borrowerId, CoinBorrowRequestPayload payload) {
        GameRound round = activeRound(roundId);
        if (borrowerId.equals(payload.lenderPlayerId())) throw new GameRuleViolationException("You cannot request coins from yourself.");
        requireActiveRoundPlayer(roundId, borrowerId); requireActiveRoundPlayer(roundId, payload.lenderPlayerId());
        long maximum = Math.multiplyExact(round.getCurrentBet(), 5);
        if (payload.amount() > maximum) throw new GameRuleViolationException("Maximum borrowing limit is " + maximum + " virtual coins.");
        Player borrower = player(borrowerId); Player lender = player(payload.lenderPlayerId());
        CoinBorrowRequest request = new CoinBorrowRequest(); request.setGameRound(round); request.setBorrower(borrower); request.setLender(lender); request.setAmount(payload.amount()); request.setRequestStatus(BorrowRequestStatus.PENDING);
        request = requestRepository.save(request);
        tableRealtimePublisher.borrowingUpdated(round.getGameTable().getTableId());
        return requestResponse(request);
    }

    @Transactional
    public CoinBorrowRequestResponse accept(Long requestId, Long lenderId) {
        CoinBorrowRequest request = lockedRequest(requestId); requirePendingForLender(request, lenderId);
        activeRound(request.getGameRound().getRoundId());
        Map<Long, Player> lockedPlayers = lockPlayers(request.getBorrower().getPlayerId(), request.getLender().getPlayerId());
        Player borrower = lockedPlayers.get(request.getBorrower().getPlayerId()); Player lender = lockedPlayers.get(request.getLender().getPlayerId());
        if (lender.getCoinBalance() < request.getAmount()) throw new GameRuleViolationException("You do not have enough virtual coins to accept this request.");
        lender.setCoinBalance(lender.getCoinBalance() - request.getAmount()); borrower.setCoinBalance(borrower.getCoinBalance() + request.getAmount()); playerRepository.saveAll(List.of(lender, borrower));
        request.setRequestStatus(BorrowRequestStatus.ACCEPTED); request.setRespondedAt(LocalDateTime.now()); requestRepository.save(request);
        PlayerDebt debt = new PlayerDebt(); debt.setBorrower(borrower); debt.setLender(lender); debt.setOriginalAmount(request.getAmount()); debt.setRemainingAmount(request.getAmount()); debt.setDebtStatus("ACTIVE"); debt = debtRepository.save(debt);
        debtTransaction(debt, request.getGameRound(), request.getAmount(), "BORROWED"); wallet(lender, request.getGameRound(), -request.getAmount(), "LOAN_OUT"); wallet(borrower, request.getGameRound(), request.getAmount(), "LOAN_IN");
        tableRealtimePublisher.borrowingUpdated(request.getGameRound().getGameTable().getTableId());
        return requestResponse(request);
    }

    @Transactional
    public CoinBorrowRequestResponse reject(Long requestId, Long lenderId) {
        CoinBorrowRequest request = lockedRequest(requestId); requirePendingForLender(request, lenderId); request.setRequestStatus(BorrowRequestStatus.REJECTED); request.setRespondedAt(LocalDateTime.now()); requestRepository.save(request);
        tableRealtimePublisher.borrowingUpdated(request.getGameRound().getGameTable().getTableId());
        return requestResponse(request);
    }

    @Transactional(readOnly = true)
    public BorrowingStateResponse state(Long playerId) {
        return new BorrowingStateResponse(requestRepository.findByLender_PlayerIdAndRequestStatusOrderByCreatedAtAsc(playerId, BorrowRequestStatus.PENDING).stream().map(this::requestResponse).toList(), requestRepository.findByBorrower_PlayerIdAndRequestStatusOrderByCreatedAtAsc(playerId, BorrowRequestStatus.PENDING).stream().map(this::requestResponse).toList(), debtRepository.findByBorrower_PlayerIdAndDebtStatusNotOrderByCreatedAtAsc(playerId, "REPAID").stream().map(this::debtResponse).toList());
    }

    @Transactional
    public void repayFromWinnings(GameRound round, Long borrowerId, long winnings) {
        List<PlayerDebt> debts = debtRepository.findWithLockByBorrower_PlayerIdAndDebtStatusNotOrderByCreatedAtAsc(borrowerId, "REPAID"); if (debts.isEmpty()) return;
        Set<Long> ids = new TreeSet<>(); ids.add(borrowerId); debts.forEach(debt -> ids.add(debt.getLender().getPlayerId())); Map<Long, Player> players = lockPlayers(ids.toArray(Long[]::new)); Player borrower = players.get(borrowerId);
        long availableFromWinnings = winnings;
        for (PlayerDebt debt : debts) { if (availableFromWinnings == 0 || borrower.getCoinBalance() == 0) break; Player lender = players.get(debt.getLender().getPlayerId()); long repaid = Math.min(Math.min(borrower.getCoinBalance(), availableFromWinnings), debt.getRemainingAmount()); borrower.setCoinBalance(borrower.getCoinBalance() - repaid); lender.setCoinBalance(lender.getCoinBalance() + repaid); availableFromWinnings -= repaid; debt.setRemainingAmount(debt.getRemainingAmount() - repaid); if (debt.getRemainingAmount() == 0) { debt.setDebtStatus("REPAID"); debt.setSettledAt(LocalDateTime.now()); } else debt.setDebtStatus("PARTIALLY_REPAID"); debtRepository.save(debt); debtTransaction(debt, round, repaid, "REPAYMENT"); wallet(borrower, round, -repaid, "DEBT_REPAYMENT"); wallet(lender, round, repaid, "DEBT_REPAYMENT"); }
        playerRepository.saveAll(players.values()); tableRealtimePublisher.borrowingUpdated(round.getGameTable().getTableId());
    }

    private GameRound activeRound(Long roundId) { GameRound round = roundRepository.findById(roundId).orElseThrow(() -> new ResourceNotFoundException("Round was not found.")); if (round.getRoundStatus() != GameRoundStatus.IN_PROGRESS) throw new GameRuleViolationException("Coin requests are available only during an active round."); return round; }
    private void requireActiveRoundPlayer(Long roundId, Long playerId) { RoundPlayer player = roundPlayerRepository.findByGameRound_RoundIdAndGamePlayer_Player_PlayerId(roundId, playerId).orElseThrow(() -> new GameRuleViolationException("Select a player at this table.")); if (player.getRoundPlayerStatus() != RoundPlayerStatus.ACTIVE) throw new GameRuleViolationException("Coin requests require active players."); }
    private CoinBorrowRequest lockedRequest(Long requestId) { return requestRepository.findWithLockByBorrowRequestId(requestId).orElseThrow(() -> new ResourceNotFoundException("Coin request was not found.")); }
    private void requirePendingForLender(CoinBorrowRequest request, Long lenderId) { if (!request.getLender().getPlayerId().equals(lenderId)) throw new GameRuleViolationException("Only the requested lender can respond."); if (request.getRequestStatus() != BorrowRequestStatus.PENDING) throw new GameRuleViolationException("This coin request has already been resolved."); }
    private Player player(Long id) { return playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player was not found.")); }
    private Map<Long, Player> lockPlayers(Long... ids) { Map<Long, Player> players = new LinkedHashMap<>(); Arrays.stream(ids).distinct().sorted().forEach(id -> players.put(id, playerRepository.findWithLockByPlayerId(id).orElseThrow(() -> new ResourceNotFoundException("Player was not found.")))); return players; }
    private void wallet(Player player, GameRound round, long amount, String type) { WalletTransaction tx = new WalletTransaction(); tx.setPlayer(player); tx.setGameRound(round); tx.setTransactionType(type); tx.setAmount(amount); tx.setBalanceAfter(player.getCoinBalance()); walletTransactionRepository.save(tx); }
    private void debtTransaction(PlayerDebt debt, GameRound round, long amount, String type) { DebtTransaction tx = new DebtTransaction(); tx.setDebt(debt); tx.setGameRound(round); tx.setAmount(amount); tx.setTransactionType(type); debtTransactionRepository.save(tx); }
    private CoinBorrowRequestResponse requestResponse(CoinBorrowRequest request) { return new CoinBorrowRequestResponse(request.getBorrowRequestId(), request.getGameRound().getRoundId(), request.getBorrower().getPlayerId(), request.getBorrower().getUsername(), request.getLender().getPlayerId(), request.getLender().getUsername(), request.getAmount(), request.getRequestStatus().name()); }
    private DebtResponse debtResponse(PlayerDebt debt) { return new DebtResponse(debt.getDebtId(), debt.getLender().getPlayerId(), debt.getLender().getUsername(), debt.getOriginalAmount(), debt.getRemainingAmount(), debt.getDebtStatus()); }
}
