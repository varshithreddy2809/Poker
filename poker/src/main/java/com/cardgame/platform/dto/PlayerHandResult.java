package com.cardgame.platform.dto;

import java.util.List;

public record PlayerHandResult(Long playerId, String username, String handCategory, List<CardResponse> cards) {
}
