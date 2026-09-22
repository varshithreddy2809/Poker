package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminCoinDistributionRequest(
        @NotNull Long targetPlayerId,
        @NotNull @Positive Long amount,
        @NotBlank String reason,
        @NotBlank String operationId) { }
