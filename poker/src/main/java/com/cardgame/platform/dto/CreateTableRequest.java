package com.cardgame.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTableRequest(
        @NotNull Long hostPlayerId,
        @NotBlank @Size(max = 100) String tableName,
        @NotNull @Positive Long entryBet,
        @NotNull @Min(2) @Max(8) Integer maxPlayers) {
}
