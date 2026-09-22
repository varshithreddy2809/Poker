package com.cardgame.platform.dto;

public record AdminTablePlayerResponse(Long playerId, String username, Integer seatNumber, String status,
                                       Long coinBalance, Long totalContribution) { }
