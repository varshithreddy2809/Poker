package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record SideShowResponseRequest(@NotBlank String response) {
}
