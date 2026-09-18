package com.cardgame.platform.dto;
import jakarta.validation.constraints.NotNull;
public record SpectateRequest(@NotNull Long selectedPlayerId) { }
