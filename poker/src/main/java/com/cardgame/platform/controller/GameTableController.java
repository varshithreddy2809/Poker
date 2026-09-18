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
    private final com.cardgame.platform.service.CoinBorrowingService coinBorrowingService;

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

    @GetMapping("/tables/{tableId}/rounds/active")
    public RoundResponse getActiveRound(@PathVariable Long tableId) {
        return friendlyTeenPattiService.getActiveRound(tableId);
    }

    @PostMapping("/rounds/{roundId}/spectate")
    public RoundResponse spectate(@PathVariable Long roundId, @Valid @RequestBody SpectateRequest request, Authentication authentication) {
        return friendlyTeenPattiService.spectate(roundId, currentPlayerId(authentication), request.selectedPlayerId());
    }

    @GetMapping("/rounds/{roundId}/players/{playerId}/hand")
    public MyHandResponse getMyHand(@PathVariable Long roundId, @PathVariable Long playerId, Authentication authentication) {
        requireCaller(authentication, playerId);
        return friendlyTeenPattiService.getMyHand(roundId, playerId);
    }

    @PostMapping("/rounds/{roundId}/visibility")
    public RoundResponse chooseVisibility(@PathVariable Long roundId, @Valid @RequestBody ChooseVisibilityRequest request,
                                          Authentication authentication) {
        return friendlyTeenPattiService.chooseVisibility(roundId, currentPlayerId(authentication), request);
    }

    @PostMapping("/rounds/{roundId}/see-cards")
    public RoundResponse seeCards(@PathVariable Long roundId, Authentication authentication) {
        return friendlyTeenPattiService.seeCards(roundId, currentPlayerId(authentication));
    }

    @PostMapping("/rounds/{roundId}/actions")
    public RoundResponse takeTurn(@PathVariable Long roundId, @Valid @RequestBody TurnActionRequest request,
                                  Authentication authentication) {
        return friendlyTeenPattiService.takeTurn(roundId, currentPlayerId(authentication), request);
    }

    @PostMapping("/rounds/{roundId}/leave")
    public RoundResponse leaveRound(@PathVariable Long roundId, Authentication authentication) {
        return friendlyTeenPattiService.leaveRound(roundId, currentPlayerId(authentication));
    }

    @PostMapping("/rounds/{roundId}/side-shows")
    public RoundResponse requestSideShow(@PathVariable Long roundId, @Valid @RequestBody SideShowRequest request,
                                         Authentication authentication) {
        return friendlyTeenPattiService.requestSideShow(roundId, currentPlayerId(authentication), request);
    }

    @PostMapping("/rounds/{roundId}/side-shows/{sideShowId}/response")
    public RoundResponse respondToSideShow(@PathVariable Long roundId, @PathVariable Long sideShowId,
                                           @Valid @RequestBody SideShowResponseRequest request, Authentication authentication) {
        return friendlyTeenPattiService.respondToSideShow(roundId, sideShowId, currentPlayerId(authentication), request);
    }

    @PostMapping("/rounds/{roundId}/coin-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public CoinBorrowRequestResponse requestCoins(@PathVariable Long roundId, @Valid @RequestBody CoinBorrowRequestPayload request,
                                                   Authentication authentication) {
        return coinBorrowingService.request(roundId, currentPlayerId(authentication), request);
    }

    @PostMapping("/coin-requests/{requestId}/accept")
    public CoinBorrowRequestResponse acceptCoinRequest(@PathVariable Long requestId, Authentication authentication) {
        return coinBorrowingService.accept(requestId, currentPlayerId(authentication));
    }

    @PostMapping("/coin-requests/{requestId}/reject")
    public CoinBorrowRequestResponse rejectCoinRequest(@PathVariable Long requestId, Authentication authentication) {
        return coinBorrowingService.reject(requestId, currentPlayerId(authentication));
    }

    @GetMapping("/coin-borrowing")
    public BorrowingStateResponse borrowingState(Authentication authentication) {
        return coinBorrowingService.state(currentPlayerId(authentication));
    }

    private void requireCaller(Authentication authentication, Long playerId) {
        gameTableService.requireCaller(authentication.getName(), playerId);
    }

    private Long currentPlayerId(Authentication authentication) {
        return gameTableService.playerIdFor(authentication.getName());
    }
}
