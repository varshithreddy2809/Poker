package com.cardgame.platform.dto;

import java.util.List;

public record GameTableResponse(
        Long tableId,
        String tableName,
        String gameType,
        Long hostPlayerId,
        Integer maxPlayers,
        String status,
        List<TablePlayerResponse> players) {
}
