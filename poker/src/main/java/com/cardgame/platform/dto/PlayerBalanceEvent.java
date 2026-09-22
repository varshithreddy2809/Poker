package com.cardgame.platform.dto;

/** Sent only through Spring's authenticated user destination. */
public record PlayerBalanceEvent(Long playerId, Long coinBalance) { }
