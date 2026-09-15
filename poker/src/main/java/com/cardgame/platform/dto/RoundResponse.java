package com.cardgame.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RoundResponse(Long roundId, Long tableId, Integer roundNumber, String status,
                            String phase, Long currentBet, Long pot, Long currentTurnPlayerId,
                            LocalDateTime visibilityDeadline, LocalDateTime turnDeadline,
                            Integer forcedSameTurnsRemaining, Long pendingSideShowId,
                            Long pendingSideShowRequesterPlayerId, Long pendingSideShowTargetPlayerId,
                            List<RoundPlayerResponse> players) {
}
