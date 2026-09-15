package com.cardgame.platform.dto;

/** A public table update. It deliberately never contains unrevealed cards. */
public record TableGameEvent(
        String type,
        Long tableId,
        RoundResponse round,
        ShowdownResponse showdown) {

    public static TableGameEvent tableUpdated(Long tableId) {
        return new TableGameEvent("TABLE_UPDATED", tableId, null, null);
    }

    public static TableGameEvent roundStarted(RoundResponse round) {
        return new TableGameEvent("ROUND_STARTED", round.tableId(), round, null);
    }

    public static TableGameEvent showdown(Long tableId, ShowdownResponse showdown) {
        return new TableGameEvent("SHOWDOWN", tableId, null, showdown);
    }
}
