package com.cardgame.platform.controller;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.service.FriendlyTeenPattiService;
import com.cardgame.platform.service.GameTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST API for the no-betting friendly Teen Patti game. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameTableController {
    private final GameTableService gameTableService;
    private final FriendlyTeenPattiService friendlyTeenPattiService;

    @PostMapping("/tables")
    @ResponseStatus(HttpStatus.CREATED)
    public GameTableResponse createTable(@Valid @RequestBody CreateTableRequest request, Authentication authentication) {
        requireCaller(authentication, request.hostPlayerId());
        return gameTableService.create(request);
    }

    @GetMapping("/tables/{tableId}")
    public GameTableResponse getTable(@PathVariable Long tableId) {
        return gameTableService.get(tableId);
    }

    @PostMapping("/tables/{tableId}/players")
    public GameTableResponse joinTable(@PathVariable Long tableId, @Valid @RequestBody JoinTableRequest request,
                                       Authentication authentication) {
        requireCaller(authentication, request.playerId());
        return gameTableService.join(tableId, request);
    }

    @PostMapping("/tables/{tableId}/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse startRound(@PathVariable Long tableId, @Valid @RequestBody StartRoundRequest request,
                                    Authentication authentication) {
        requireCaller(authentication, request.hostPlayerId());
        return friendlyTeenPattiService.startRound(tableId, request);
    }

    @GetMapping("/rounds/{roundId}/players/{playerId}/hand")
    public MyHandResponse getMyHand(@PathVariable Long roundId, @PathVariable Long playerId, Authentication authentication) {
        requireCaller(authentication, playerId);
        return friendlyTeenPattiService.getMyHand(roundId, playerId);
    }

    @PostMapping("/rounds/{roundId}/showdown")
    public ShowdownResponse showdown(@PathVariable Long roundId, @Valid @RequestBody ShowdownRequest request,
                                     Authentication authentication) {
        requireCaller(authentication, request.hostPlayerId());
        return friendlyTeenPattiService.showdown(roundId, request);
    }

    private void requireCaller(Authentication authentication, Long playerId) {
        gameTableService.requireCaller(authentication.getName(), playerId);
    }
}
