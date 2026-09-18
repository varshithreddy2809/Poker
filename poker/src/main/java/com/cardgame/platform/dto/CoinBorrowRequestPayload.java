package com.cardgame.platform.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record CoinBorrowRequestPayload(@NotNull Long lenderPlayerId, @NotNull @Positive Long amount) { }
