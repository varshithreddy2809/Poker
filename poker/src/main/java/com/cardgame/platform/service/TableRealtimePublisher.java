package com.cardgame.platform.service;

import com.cardgame.platform.dto.TableGameEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TableRealtimePublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void tableUpdated(Long tableId) {
        applicationEventPublisher.publishEvent(TableGameEvent.tableUpdated(tableId));
    }

    /** Signals clients to refresh their authenticated borrowing state. */
    public void borrowingUpdated(Long tableId) {
        applicationEventPublisher.publishEvent(TableGameEvent.borrowingUpdated(tableId));
    }

    public void roundStarted(com.cardgame.platform.dto.RoundResponse round) {
        applicationEventPublisher.publishEvent(TableGameEvent.roundStarted(round));
    }

    public void showdown(Long tableId, com.cardgame.platform.dto.ShowdownResponse showdown) {
        applicationEventPublisher.publishEvent(TableGameEvent.showdown(tableId, showdown));
    }
}
