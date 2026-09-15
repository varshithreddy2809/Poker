package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotNull;

public record SideShowRequest(@NotNull Long targetPlayerId) {
}
