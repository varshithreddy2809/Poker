package com.cardgame.platform.controller;

import com.cardgame.platform.dto.PlayerResponse;
import com.cardgame.platform.dto.RegisterPlayerRequest;
import com.cardgame.platform.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse register(@Valid @RequestBody RegisterPlayerRequest request) {
        return playerService.register(request);
    }

    @GetMapping("/me")
    public PlayerResponse getCurrentPlayer(Authentication authentication) {
        return playerService.getCurrentPlayer(authentication.getName());
    }

    @GetMapping("/{playerId}")
    public PlayerResponse get(@PathVariable Long playerId, Authentication authentication) {
        playerService.requireCaller(authentication.getName(), playerId);
        return playerService.getPlayer(playerId);
    }
}
