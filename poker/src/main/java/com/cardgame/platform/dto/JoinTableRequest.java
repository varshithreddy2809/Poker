package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotNull;

public record JoinTableRequest(@NotNull Long playerId) {
}
