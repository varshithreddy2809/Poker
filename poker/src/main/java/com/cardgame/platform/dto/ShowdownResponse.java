package com.cardgame.platform.dto;

import java.util.List;

public record ShowdownResponse(Long roundId, Long winnerPlayerId, String winnerUsername,
                               String winningHandCategory, List<PlayerHandResult> hands) {
}
