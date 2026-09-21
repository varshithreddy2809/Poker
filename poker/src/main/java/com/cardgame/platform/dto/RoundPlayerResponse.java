package com.cardgame.platform.dto;

public record RoundPlayerResponse(Long playerId, String username, Integer seatNumber, String visibility,
                                  String status, Long totalContribution, Integer sameBetActions,
                                  Integer finalBetTurns, boolean live) {
}
