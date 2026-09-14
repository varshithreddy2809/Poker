package com.cardgame.platform.dto;

public record RoundResponse(Long roundId, Long tableId, Integer roundNumber, String status) {
}
