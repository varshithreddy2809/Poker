package com.cardgame.platform.service;

import com.cardgame.platform.dto.PlayerResponse;
import com.cardgame.platform.dto.RegisterPlayerRequest;
import com.cardgame.platform.entity.AccountStatus;
import com.cardgame.platform.entity.Player;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.exception.ResourceNotFoundException;
import com.cardgame.platform.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService implements UserDetailsService {
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PlayerResponse register(RegisterPlayerRequest request) {
        if (playerRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new GameRuleViolationException("That username is already in use.");
        }
        if (playerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new GameRuleViolationException("That email is already in use.");
        }

        Player player = new Player();
        player.setUsername(request.username().trim());
        player.setEmail(request.email().trim().toLowerCase());
        player.setPasswordHash(passwordEncoder.encode(request.password()));
        // Development-friendly virtual starting balance. No real money is used.
        player.setCoinBalance(10_000L);
        player.setAccountStatus(AccountStatus.ACTIVE);
        return toResponse(playerRepository.save(player));
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayer(Long playerId) {
        return toResponse(getActivePlayer(playerId));
    }

    public Player getActivePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player " + playerId + " was not found."));
        if (player.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new GameRuleViolationException("This player account is not active.");
        }
        return player;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown player."));
        return User.withUsername(player.getUsername())
                .password(player.getPasswordHash())
                .authorities("PLAYER")
                .disabled(player.getAccountStatus() != AccountStatus.ACTIVE)
                .build();
    }

    @Transactional(readOnly = true)
    public void requireCaller(String username, Long claimedPlayerId) {
        Player player = playerRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated player was not found."));
        if (!player.getPlayerId().equals(claimedPlayerId)) {
            throw new GameRuleViolationException("You may only act as your own player account.");
        }
    }

    @Transactional(readOnly = true)
    public Long playerIdFor(String username) {
        return playerRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated player was not found."))
                .getPlayerId();
    }

    private PlayerResponse toResponse(Player player) {
        return new PlayerResponse(player.getPlayerId(), player.getUsername(), player.getEmail(),
                player.getCoinBalance(), player.getAccountStatus().name());
    }
}
