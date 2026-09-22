package com.cardgame.platform.service;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.exception.ResourceNotFoundException;
import com.cardgame.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final long MAX_DISTRIBUTION = 1_000_000L;

    private final PlayerRepository playerRepository;
    private final GameTableRepository gameTableRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameRoundRepository gameRoundRepository;
    private final RoundPlayerRepository roundPlayerRepository;
    private final AdminCoinTransactionRepository transactionRepository;
    private final TableRealtimePublisher tableRealtimePublisher;
    private final PlayerBalanceRealtimePublisher playerBalanceRealtimePublisher;

    @Transactional(readOnly = true)
    public List<AdminTableResponse> tables() {
        return gameTableRepository.findAll().stream().map(this::tableResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminTableResponse table(Long tableId) {
        return tableResponse(requireTable(tableId));
    }

    @Transactional(readOnly = true)
    public List<AdminCoinTransactionResponse> history() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream().map(this::transactionResponse).toList();
    }

    @Transactional
    public List<AdminCoinTransactionResponse> distributeToPlayer(String username, AdminCoinDistributionRequest request) {
        Player admin = admin(username);
        validate(request.amount(), request.reason(), request.operationId());
        Player target = activePlayer(request.targetPlayerId());
        return List.of(distribute(admin, target, null, request.amount(), request.reason(), request.operationId(), "PLAYER"));
    }

    @Transactional
    public List<AdminCoinTransactionResponse> distributeToTable(String username, Long tableId,
                                                                 AdminTableCoinDistributionRequest request) {
        Player admin = admin(username);
        validate(request.amount(), request.reason(), request.operationId());
        GameTable table = requireTable(tableId);
        List<GamePlayer> seats = gamePlayerRepository.findByGameTable_TableIdAndPlayerStatusOrderBySeatNumber(tableId, GamePlayerStatus.JOINED);
        if (seats.isEmpty()) throw new GameRuleViolationException("This table has no seated players.");
        List<AdminCoinTransactionResponse> responses = new ArrayList<>();
        for (GamePlayer seat : seats) {
            responses.add(distribute(admin, activePlayer(seat.getPlayer().getPlayerId()), table, request.amount(),
                    request.reason(), request.operationId(), "TABLE"));
        }
        tableRealtimePublisher.tableUpdated(tableId);
        return responses;
    }

    private AdminCoinTransactionResponse distribute(Player admin, Player target, GameTable table, long amount,
                                                     String reason, String operationId, String operationType) {
        Optional<AdminCoinTransaction> previous = transactionRepository
                .findByOperationIdAndTargetPlayer_PlayerId(operationId, target.getPlayerId());
        if (previous.isPresent()) return transactionResponse(previous.get());
        try {
            target.setCoinBalance(Math.addExact(target.getCoinBalance(), amount));
        } catch (ArithmeticException exception) {
            throw new GameRuleViolationException("Coin distribution would exceed the supported balance.");
        }
        playerRepository.save(target);
        AdminCoinTransaction transaction = new AdminCoinTransaction();
        transaction.setAdmin(admin); transaction.setTargetPlayer(target); transaction.setGameTable(table);
        transaction.setOperationId(operationId.trim()); transaction.setOperationType(operationType);
        transaction.setAmount(amount); transaction.setReason(reason.trim());
        AdminCoinTransactionResponse response = transactionResponse(transactionRepository.save(transaction));
        playerBalanceRealtimePublisher.balanceUpdated(target.getPlayerId(), target.getCoinBalance());
        // Existing table subscriptions refresh their authoritative profile after this public table update.
        gamePlayerRepository.findByPlayer_PlayerId(target.getPlayerId()).stream()
                .map(seat -> seat.getGameTable().getTableId()).distinct().forEach(tableRealtimePublisher::tableUpdated);
        return response;
    }

    private void validate(long amount, String reason, String operationId) {
        if (amount <= 0 || amount > MAX_DISTRIBUTION) throw new GameRuleViolationException("Amount must be between 1 and " + MAX_DISTRIBUTION + " virtual coins.");
        if (reason == null || reason.isBlank() || reason.trim().length() > 250) throw new GameRuleViolationException("A reason of up to 250 characters is required.");
        if (operationId == null || operationId.isBlank() || operationId.trim().length() > 80) throw new GameRuleViolationException("A valid distribution operation ID is required.");
    }

    private Player admin(String username) {
        Player player = playerRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated administrator was not found."));
        if (player.getAccountRole() != AccountRole.ADMIN) throw new GameRuleViolationException("Administrator access is required.");
        return player;
    }
    private Player activePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResourceNotFoundException("Player " + playerId + " was not found."));
        if (player.getAccountStatus() != AccountStatus.ACTIVE) throw new GameRuleViolationException("This player account is not active.");
        return player;
    }
    private GameTable requireTable(Long tableId) { return gameTableRepository.findById(tableId).orElseThrow(() -> new ResourceNotFoundException("Table " + tableId + " was not found.")); }
    private AdminTableResponse tableResponse(GameTable table) {
        Optional<GameRound> latest = gameRoundRepository.findTopByGameTable_TableIdOrderByRoundNumberDesc(table.getTableId());
        Map<Long, RoundPlayer> states = latest.map(round -> roundPlayerRepository.findByGameRound_RoundIdOrderByGamePlayer_SeatNumber(round.getRoundId()).stream()
                .collect(java.util.stream.Collectors.toMap(state -> state.getGamePlayer().getGamePlayerId(), state -> state))).orElseGet(Map::of);
        List<AdminTablePlayerResponse> players = gamePlayerRepository.findByGameTable_TableIdOrderBySeatNumber(table.getTableId()).stream()
                .map(seat -> new AdminTablePlayerResponse(seat.getPlayer().getPlayerId(), seat.getPlayer().getUsername(), seat.getSeatNumber().intValue(),
                        seat.getPlayerStatus().name(), seat.getPlayer().getCoinBalance(),
                        states.containsKey(seat.getGamePlayerId()) ? states.get(seat.getGamePlayerId()).getTotalContribution() : 0L)).toList();
        return new AdminTableResponse(table.getTableId(), table.getTableName(), table.getTableStatus().name(), table.getEntryBet(),
                latest.map(GameRound::getRoundId).orElse(null), latest.map(round -> round.getPhase().name()).orElse(null),
                latest.map(GameRound::getCurrentBet).orElse(null), latest.map(GameRound::getPot).orElse(null), players);
    }
    private AdminCoinTransactionResponse transactionResponse(AdminCoinTransaction transaction) {
        return new AdminCoinTransactionResponse(transaction.getTransactionId(), transaction.getAdmin().getPlayerId(), transaction.getAdmin().getUsername(),
                transaction.getTargetPlayer().getPlayerId(), transaction.getTargetPlayer().getUsername(),
                transaction.getGameTable() == null ? null : transaction.getGameTable().getTableId(), transaction.getOperationType(),
                transaction.getAmount(), transaction.getReason(), transaction.getCreatedAt());
    }
}
