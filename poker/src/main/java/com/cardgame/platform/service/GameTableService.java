package com.cardgame.platform.service;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.exception.ResourceNotFoundException;
import com.cardgame.platform.repository.GamePlayerRepository;
import com.cardgame.platform.repository.GameTableRepository;
import com.cardgame.platform.repository.GameTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameTableService {
    private static final String TEEN_PATTI = "TEEN_PATTI";

    private final GameTableRepository gameTableRepository;
    private final GameTypeRepository gameTypeRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final PlayerService playerService;
    private final TableRealtimePublisher tableRealtimePublisher;

    @Transactional
    public GameTableResponse create(CreateTableRequest request) {
        Player host = playerService.getActivePlayer(request.hostPlayerId());
        GameType gameType = gameTypeRepository.findByCodeAndActiveTrue(TEEN_PATTI)
                .orElseThrow(() -> new ResourceNotFoundException("The TEEN_PATTI game type has not been seeded."));

        if (request.maxPlayers() < gameType.getMinPlayers() || request.maxPlayers() > gameType.getMaxPlayers()) {
            throw new GameRuleViolationException("Teen Patti tables must allow " + gameType.getMinPlayers()
                    + " to " + gameType.getMaxPlayers() + " players.");
        }

        GameTable gameTable = new GameTable();
        gameTable.setGameType(gameType);
        gameTable.setHostPlayer(host);
        gameTable.setTableName(request.tableName().trim());
        gameTable.setMaxPlayers(request.maxPlayers().byteValue());
        gameTable.setEntryBet(request.entryBet());
        gameTable.setTableStatus(GameTableStatus.OPEN);
        gameTable = gameTableRepository.save(gameTable);

        GamePlayer hostSeat = new GamePlayer();
        hostSeat.setGameTable(gameTable);
        hostSeat.setPlayer(host);
        hostSeat.setSeatNumber((byte) 1);
        hostSeat.setPlayerStatus(GamePlayerStatus.JOINED);
        gamePlayerRepository.save(hostSeat);

        GameTableResponse response = toResponse(gameTable, List.of(hostSeat));
        tableRealtimePublisher.tableUpdated(gameTable.getTableId());
        return response;
    }

    @Transactional
    public GameTableResponse join(Long tableId, JoinTableRequest request) {
        GameTable gameTable = getTable(tableId);
        if (gameTable.getTableStatus() != GameTableStatus.OPEN) {
            throw new GameRuleViolationException("Players cannot join while this table is in a round.");
        }
        if (gamePlayerRepository.existsByGameTable_TableIdAndPlayer_PlayerId(tableId, request.playerId())) {
            throw new GameRuleViolationException("This player already has a seat at the table.");
        }

        List<GamePlayer> currentPlayers = gamePlayerRepository
                .findByGameTable_TableIdAndPlayerStatusOrderBySeatNumber(tableId, GamePlayerStatus.JOINED);
        if (currentPlayers.size() >= gameTable.getMaxPlayers()) {
            throw new GameRuleViolationException("This table is full.");
        }
        Player player = playerService.getActivePlayer(request.playerId());
        Set<Integer> occupiedSeats = currentPlayers.stream()
                .map(participant -> participant.getSeatNumber().intValue())
                .collect(Collectors.toSet());
        int seat = firstFreeSeat(occupiedSeats, gameTable.getMaxPlayers().intValue());

        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGameTable(gameTable);
        gamePlayer.setPlayer(player);
        gamePlayer.setSeatNumber((byte) seat);
        gamePlayer.setPlayerStatus(GamePlayerStatus.JOINED);
        gamePlayerRepository.save(gamePlayer);

        currentPlayers.add(gamePlayer);
        GameTableResponse response = toResponse(gameTable, currentPlayers);
        tableRealtimePublisher.tableUpdated(tableId);
        return response;
    }

    @Transactional(readOnly = true)
    public GameTableResponse get(Long tableId) {
        GameTable gameTable = getTable(tableId);
        return toResponse(gameTable, gamePlayerRepository.findByGameTable_TableIdOrderBySeatNumber(tableId));
    }

    public GameTable getTable(Long tableId) {
        return gameTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table " + tableId + " was not found."));
    }

    public void requireCaller(String username, Long playerId) {
        playerService.requireCaller(username, playerId);
    }

    public Long playerIdFor(String username) {
        return playerService.playerIdFor(username);
    }

    private int firstFreeSeat(Set<Integer> occupiedSeats, int maxPlayers) {
        for (int seat = 1; seat <= maxPlayers; seat++) {
            if (!occupiedSeats.contains(seat)) {
                return seat;
            }
        }
        throw new GameRuleViolationException("This table has no available seat.");
    }

    private GameTableResponse toResponse(GameTable table, List<GamePlayer> players) {
        List<TablePlayerResponse> playerResponses = players.stream()
                .map(player -> new TablePlayerResponse(player.getPlayer().getPlayerId(), player.getPlayer().getUsername(),
                        player.getSeatNumber().intValue(), player.getPlayerStatus().name()))
                .toList();
        return new GameTableResponse(table.getTableId(), table.getTableName(), table.getGameType().getCode(),
                table.getHostPlayer().getPlayerId(), table.getEntryBet(), table.getMaxPlayers().intValue(),
                table.getTableStatus().name(), playerResponses);
    }
}
