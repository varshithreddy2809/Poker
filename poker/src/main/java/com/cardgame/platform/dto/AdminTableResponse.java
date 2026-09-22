package com.cardgame.platform.dto;

import java.util.List;

/** Administrative view: no card data is ever included. */
public record AdminTableResponse(Long tableId, String tableName, String status, Long entryBet,
                                 Long roundId, String phase, Long currentBet, Long pot,
                                 List<AdminTablePlayerResponse> players) { }
