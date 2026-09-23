package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotNull;

/** The authenticated player's desired waiting-lobby readiness. */
public record ReadyRequest(@NotNull Boolean ready) {
}
