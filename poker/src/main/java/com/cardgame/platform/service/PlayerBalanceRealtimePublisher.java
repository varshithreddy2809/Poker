package com.cardgame.platform.service;

import com.cardgame.platform.dto.PlayerBalanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerBalanceRealtimePublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    public void balanceUpdated(Long playerId, Long balance) { applicationEventPublisher.publishEvent(new PlayerBalanceEvent(playerId, balance)); }
}
