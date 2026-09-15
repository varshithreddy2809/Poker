package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record TurnActionRequest(@NotBlank String action, Long amount) {
}
