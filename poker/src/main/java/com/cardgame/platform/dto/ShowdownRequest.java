package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotNull;

public record ShowdownRequest(@NotNull Long hostPlayerId) {
}
