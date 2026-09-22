package com.cardgame.platform.service;

import com.cardgame.platform.dto.PlayerResponse;
import com.cardgame.platform.entity.AccountStatus;
import com.cardgame.platform.entity.Player;
import com.cardgame.platform.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerServiceAuthenticationTest {
    @Test
    void existingAccountsCanAuthenticateThroughANewServiceInstance() {
        PlayerRepository repository = mock(PlayerRepository.class);
        Player alice = player(1L, "Alice", "alice@example.com", "alice-password");
        Player bob = player(2L, "Bob", "bob@example.com", "bob-password");
        Map<String, Player> accounts = Map.of("alice", alice, "bob", bob);
        when(repository.findByUsernameIgnoreCase(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(accounts.get(invocation.getArgument(0, String.class).toLowerCase())));

        PlayerService restartedService = new PlayerService(repository, new BCryptPasswordEncoder());

        PlayerResponse signedInAlice = restartedService.getCurrentPlayer("Alice");
        PlayerResponse signedInBob = restartedService.getCurrentPlayer("Bob");

        assertEquals(1L, signedInAlice.playerId());
        assertEquals(2L, signedInBob.playerId());
        assertTrue(new BCryptPasswordEncoder().matches("alice-password", restartedService.loadUserByUsername("Alice").getPassword()));
        assertTrue(new BCryptPasswordEncoder().matches("bob-password", restartedService.loadUserByUsername("Bob").getPassword()));
    }

    private static Player player(Long id, String username, String email, String password) {
        Player player = new Player();
        player.setPlayerId(id); player.setUsername(username); player.setEmail(email);
        player.setPasswordHash(new BCryptPasswordEncoder().encode(password)); player.setCoinBalance(10_000L);
        player.setAccountStatus(AccountStatus.ACTIVE);
        return player;
    }
}
