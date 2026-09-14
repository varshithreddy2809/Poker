package com.cardgame.platform.dto;

public record TablePlayerResponse(Long playerId, String username, Integer seatNumber, String status) {
}
