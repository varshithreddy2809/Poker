package com.cardgame.platform.service;

import com.cardgame.platform.dto.TableGameEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TableRealtimeEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(TableGameEvent event) {
        messagingTemplate.convertAndSend("/topic/tables/" + event.tableId(), event);
    }
}
