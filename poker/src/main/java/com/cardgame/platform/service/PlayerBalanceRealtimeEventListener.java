package com.cardgame.platform.service;

import com.cardgame.platform.dto.PlayerBalanceEvent;
import com.cardgame.platform.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlayerBalanceRealtimeEventListener {
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(PlayerBalanceEvent event) {
        playerRepository.findById(event.playerId()).ifPresent(player ->
                messagingTemplate.convertAndSendToUser(player.getUsername(), "/queue/balance", event));
    }
}
