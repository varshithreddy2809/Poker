package com.cardgame.platform.dto;

import java.util.List;

public record MyHandResponse(Long roundId, Long playerId, List<CardResponse> cards) {
}
