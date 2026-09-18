package com.cardgame.platform.service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
@Component @RequiredArgsConstructor
public class WebSocketConnectionListener {
    private final PlayerConnectionService playerConnectionService;
    @EventListener public void connected(SessionConnectEvent event) { if (event.getUser() != null) playerConnectionService.reconnected(event.getUser().getName()); }
    @EventListener public void disconnected(SessionDisconnectEvent event) { if (event.getUser() != null) playerConnectionService.disconnected(event.getUser().getName()); }
}
