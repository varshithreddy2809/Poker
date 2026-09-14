package com.cardgame.platform.dto;

public record PlayerResponse(Long playerId, String username, String email, Long coinBalance, String accountStatus) {
}
