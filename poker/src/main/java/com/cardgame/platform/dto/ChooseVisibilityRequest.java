package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record ChooseVisibilityRequest(@NotBlank String choice) {
}
