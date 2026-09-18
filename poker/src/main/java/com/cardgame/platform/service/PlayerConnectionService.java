package com.cardgame.platform.service;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.repository.RoundPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service @RequiredArgsConstructor
public class PlayerConnectionService {
    private final RoundPlayerRepository roundPlayerRepository; private final TableRealtimePublisher tableRealtimePublisher; private final FriendlyTeenPattiService teenPattiService;
    @Transactional public void disconnected(String username) { LocalDateTime now = LocalDateTime.now(); roundPlayerRepository.findByGameRound_RoundStatusAndGamePlayer_Player_UsernameIgnoreCase(GameRoundStatus.IN_PROGRESS, username).stream().filter(p -> p.getRoundPlayerStatus() == RoundPlayerStatus.ACTIVE).forEach(p -> { p.setConnectionStatus(PlayerConnectionStatus.DISCONNECTED); p.setDisconnectedAt(now); p.setReconnectDeadline(now.plusSeconds(60)); tableRealtimePublisher.tableUpdated(p.getGameRound().getGameTable().getTableId()); }); }
    @Transactional public void reconnected(String username) { LocalDateTime now = LocalDateTime.now(); roundPlayerRepository.findByGameRound_RoundStatusAndGamePlayer_Player_UsernameIgnoreCase(GameRoundStatus.IN_PROGRESS, username).forEach(p -> { if (p.getConnectionStatus() != PlayerConnectionStatus.DISCONNECTED) return; if (!p.getReconnectDeadline().isAfter(now)) { teenPattiService.dropDisconnectedPlayer(p.getGameRound().getRoundId(), p.getGamePlayer().getPlayer().getPlayerId()); return; } p.setConnectionStatus(PlayerConnectionStatus.CONNECTED); p.setDisconnectedAt(null); p.setReconnectDeadline(null); tableRealtimePublisher.tableUpdated(p.getGameRound().getGameTable().getTableId()); }); }
    @Transactional public void expireReconnectWindows() { roundPlayerRepository.findByConnectionStatusAndReconnectDeadlineBefore(PlayerConnectionStatus.DISCONNECTED, LocalDateTime.now()).forEach(p -> teenPattiService.dropDisconnectedPlayer(p.getGameRound().getRoundId(), p.getGamePlayer().getPlayer().getPlayerId())); }
}
