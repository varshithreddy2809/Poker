package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotNull;

public record StartRoundRequest(@NotNull Long hostPlayerId) {
}
