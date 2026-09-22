package com.cardgame.platform.service;

import com.cardgame.platform.dto.AdminCoinDistributionRequest;
import com.cardgame.platform.entity.AccountRole;
import com.cardgame.platform.entity.AccountStatus;
import com.cardgame.platform.entity.Player;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.repository.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {
    @Test
    void adminDistributionCreditsOnlyAvailableBalanceAndCreatesAuditEntry() {
        Player admin = player(1L, "admin", 10_000L, AccountRole.ADMIN);
        Player target = player(2L, "player", 500L, AccountRole.PLAYER);
        PlayerRepository players = mock(PlayerRepository.class);
        AdminCoinTransactionRepository transactions = mock(AdminCoinTransactionRepository.class);
        GamePlayerRepository seats = mock(GamePlayerRepository.class);
        when(players.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));
        when(players.findById(2L)).thenReturn(Optional.of(target));
        when(transactions.findByOperationIdAndTargetPlayer_PlayerId("request-1", 2L)).thenReturn(Optional.empty());
        when(transactions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(seats.findByPlayer_PlayerId(2L)).thenReturn(List.of());

        AdminService service = service(players, transactions, seats);
        service.distributeToPlayer("admin", new AdminCoinDistributionRequest(2L, 250L, "Reward", "request-1"));

        assertEquals(750L, target.getCoinBalance());
        verify(players).save(target);
        verify(transactions).save(any());
    }

    @Test
    void zeroAndNegativeAwardsAreRejectedBeforeAnyBalanceChange() {
        PlayerRepository players = mock(PlayerRepository.class);
        AdminCoinTransactionRepository transactions = mock(AdminCoinTransactionRepository.class);
        GamePlayerRepository seats = mock(GamePlayerRepository.class);
        Player admin = player(1L, "admin", 10_000L, AccountRole.ADMIN);
        when(players.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));
        AdminService service = service(players, transactions, seats);

        assertThrows(GameRuleViolationException.class, () -> service.distributeToPlayer("admin", new AdminCoinDistributionRequest(2L, 0L, "Reward", "request-2")));
        assertThrows(GameRuleViolationException.class, () -> service.distributeToPlayer("admin", new AdminCoinDistributionRequest(2L, -1L, "Reward", "request-3")));
        verify(players, never()).save(any());
    }

    @Test
    void playerIdentityCannotSelfAuthorizeAsAdmin() {
        PlayerRepository players = mock(PlayerRepository.class);
        Player regular = player(1L, "player", 10_000L, AccountRole.PLAYER);
        when(players.findByUsernameIgnoreCase("player")).thenReturn(Optional.of(regular));
        AdminService service = service(players, mock(AdminCoinTransactionRepository.class), mock(GamePlayerRepository.class));

        assertThrows(GameRuleViolationException.class, () -> service.distributeToPlayer("player", new AdminCoinDistributionRequest(2L, 100L, "Forged role", "request-4")));
    }

    private static AdminService service(PlayerRepository players, AdminCoinTransactionRepository transactions, GamePlayerRepository seats) {
        return new AdminService(players, mock(GameTableRepository.class), seats, mock(GameRoundRepository.class),
                mock(RoundPlayerRepository.class), transactions, mock(TableRealtimePublisher.class), mock(PlayerBalanceRealtimePublisher.class));
    }
    private static Player player(Long id, String username, Long balance, AccountRole role) {
        Player player = new Player(); player.setPlayerId(id); player.setUsername(username); player.setCoinBalance(balance);
        player.setAccountStatus(AccountStatus.ACTIVE); player.setAccountRole(role); return player;
    }
}
