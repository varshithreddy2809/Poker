package com.cardgame.platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameTimerService {
    private final FriendlyTeenPattiService teenPattiService;
    private final PlayerConnectionService playerConnectionService;

    @Scheduled(fixedDelay = 1000)
    public void enforceRoundDeadlines() {
        teenPattiService.processTimeouts();
        playerConnectionService.expireReconnectWindows();
    }
}
