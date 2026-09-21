package com.cardgame.platform.service;

import com.cardgame.platform.dto.RoundResponse;
import com.cardgame.platform.dto.TurnActionRequest;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.repository.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

class FriendlyTeenPattiServiceBettingRulesTest {
    @Test
    void sameLimitIsPerPlayerAndRaiseResetsEveryActivePlayer() {
        Fixture fixture = new Fixture();
        fixture.round.setCurrentBet(5L);
        fixture.a.setSameBetActions((byte) 5);
        fixture.b.setSameBetActions((byte) 4);

        assertThrows(GameRuleViolationException.class,
                () -> fixture.service.takeTurn(1L, 1L, new TurnActionRequest("SAME", null)));
        assertEquals(4, fixture.b.getSameBetActions().intValue(), "one player's limit must not affect another player");

        fixture.service.takeTurn(1L, 1L, new TurnActionRequest("RAISE", 6L));
        assertEquals(0, fixture.a.getSameBetActions().intValue());
        assertEquals(0, fixture.b.getSameBetActions().intValue());
        assertEquals(6L, fixture.round.getCurrentBet());
    }

    @Test
    void samePublishesTheUpdatedAuthoritativeCounterForEveryPlayer() {
        Fixture fixture = new Fixture();

        RoundResponse response = fixture.service.takeTurn(1L, 1L, new TurnActionRequest("SAME", null));

        assertEquals(1, fixture.a.getSameBetActions().intValue());
        assertEquals(0, fixture.b.getSameBetActions().intValue());
        assertEquals(1, response.players().get(0).sameBetActions());
        assertEquals(0, response.players().get(1).sameBetActions());
        ArgumentCaptor<RoundResponse> broadcast = ArgumentCaptor.forClass(RoundResponse.class);
        verify(fixture.publisher).roundStarted(broadcast.capture());
        assertEquals(1, broadcast.getValue().players().get(0).sameBetActions());
        assertEquals(0, broadcast.getValue().players().get(1).sameBetActions());
    }

    @Test
    void roundResponsePublishesPublicVisibilityAndCountersWithoutCards() {
        Fixture fixture = new Fixture();
        fixture.a.setVisibilityStatus(VisibilityStatus.SEEN);
        fixture.a.setSameBetActions((byte) 3);
        fixture.b.setVisibilityStatus(VisibilityStatus.BLIND);
        fixture.b.setSameBetActions((byte) 2);

        RoundResponse response = fixture.service.getActiveRound(99L);

        assertEquals("SEEN", response.players().get(0).visibility());
        assertEquals("BLIND", response.players().get(1).visibility());
        assertEquals(3, response.players().get(0).sameBetActions());
        assertEquals(2, response.players().get(1).sameBetActions());
        assertFalse(java.util.Arrays.stream(RoundResponse.class.getRecordComponents())
                .anyMatch(component -> component.getName().toLowerCase().contains("card")),
                "the public round state must never include private cards");
    }

    @Test
    void finalTurnsAreIndependentAndACompletedPlayerCannotBetAgain() {
        Fixture fixture = new Fixture();
        fixture.round.setPhase(GamePhase.FINAL_TWO);
        fixture.round.setFinalTwoStarted(true);
        fixture.a.setFinalBetTurns((byte) 4);
        fixture.b.setFinalBetTurns((byte) 0);

        fixture.service.takeTurn(1L, 1L, new TurnActionRequest("SAME", null));

        assertEquals(5, fixture.a.getFinalBetTurns().intValue());
        assertEquals(0, fixture.b.getFinalBetTurns().intValue());
        fixture.round.setCurrentTurnGamePlayer(fixture.a.getGamePlayer());
        assertThrows(GameRuleViolationException.class,
                () -> fixture.service.takeTurn(1L, 1L, new TurnActionRequest("SAME", null)));
    }

    @Test
    void reachingTheOriginalEntryBetThresholdEntersAndPublishesFinalTwo() {
        Fixture fixture = new Fixture();
        fixture.round.setCurrentBet(1_499L);

        RoundResponse response = fixture.service.takeTurn(1L, 1L, new TurnActionRequest("RAISE", 1_500L));

        assertEquals(1_500L, response.finalTwoThreshold());
        assertEquals(GamePhase.FINAL_TWO.name(), response.phase());
        assertTrue(fixture.round.getFinalTwoStarted());
        assertEquals(0, fixture.a.getFinalBetTurns().intValue());
        assertEquals(0, fixture.b.getFinalBetTurns().intValue());
        ArgumentCaptor<RoundResponse> broadcast = ArgumentCaptor.forClass(RoundResponse.class);
        verify(fixture.publisher).roundStarted(broadcast.capture());
        assertEquals(GamePhase.FINAL_TWO.name(), broadcast.getValue().phase());
    }

    @Test
    void aStaleBettingPhaseIsRestoredToFinalTwoAtTheThreshold() {
        Fixture fixture = new Fixture();
        fixture.round.setCurrentBet(1_500L);
        fixture.round.setFinalTwoStarted(true);

        RoundResponse response = fixture.service.takeTurn(1L, 1L, new TurnActionRequest("RAISE", 1_501L));

        assertEquals(GamePhase.FINAL_TWO.name(), response.phase());
    }

    @Test
    void finalTwoAutomaticallySettlesAfterBothPlayersUseFiveTurns() {
        Fixture fixture = new Fixture();
        fixture.round.setPhase(GamePhase.FINAL_TWO);
        fixture.round.setFinalTwoStarted(true);
        fixture.a.setFinalBetTurns((byte) 4);
        fixture.b.setFinalBetTurns((byte) 4);

        fixture.service.takeTurn(1L, 1L, new TurnActionRequest("SAME", null));
        assertEquals(5, fixture.a.getFinalBetTurns().intValue());
        assertEquals(GameRoundStatus.IN_PROGRESS, fixture.round.getRoundStatus());

        fixture.service.takeTurn(1L, 2L, new TurnActionRequest("SAME", null));

        assertEquals(5, fixture.b.getFinalBetTurns().intValue());
        assertEquals(GameRoundStatus.COMPLETED, fixture.round.getRoundStatus());
        verify(fixture.publisher).showdown(any(), any());
    }

    @Test
    void droppingDuringFinalTwoImmediatelyAwardsTheRemainingPlayer() {
        Fixture fixture = new Fixture();
        fixture.round.setPhase(GamePhase.FINAL_TWO);
        fixture.round.setFinalTwoStarted(true);
        when(fixture.roundPlayers.findByGameRound_RoundIdAndRoundPlayerStatusOrderByGamePlayer_SeatNumber(1L, RoundPlayerStatus.ACTIVE)).thenReturn(List.of(fixture.b));

        fixture.service.leaveRound(1L, 1L);

        assertEquals(GameRoundStatus.COMPLETED, fixture.round.getRoundStatus());
        verify(fixture.publisher).showdown(any(), any());
    }

    @Test
    void showWithAHigherBetSettlesImmediatelyAndBroadcastsTheCompletedAuthoritativeState() {
        Fixture fixture = new Fixture();
        fixture.round.setCurrentBet(2L);

        RoundResponse response = fixture.service.takeTurn(1L, 1L, new TurnActionRequest("SHOW", 3L));

        assertEquals(3L, fixture.round.getCurrentBet());
        assertEquals(3L, fixture.round.getPot());
        assertEquals(GameRoundStatus.COMPLETED, fixture.round.getRoundStatus());
        assertEquals(GamePhase.COMPLETED.name(), response.phase());
        ArgumentCaptor<RoundResponse> completedRound = ArgumentCaptor.forClass(RoundResponse.class);
        verify(fixture.publisher).showdown(completedRound.capture(), any());
        assertEquals(GamePhase.COMPLETED.name(), completedRound.getValue().phase());
        assertEquals(3L, completedRound.getValue().currentBet());
        assertEquals(3L, completedRound.getValue().pot());
        verify(fixture.publisher, never()).roundStarted(any());
    }

    private static final class Fixture {
        final GameRoundRepository rounds = mock(GameRoundRepository.class);
        final RoundPlayerRepository roundPlayers = mock(RoundPlayerRepository.class);
        final SideShowRepository sideShows = mock(SideShowRepository.class);
        final PlayerCardRepository cards = mock(PlayerCardRepository.class);
        final TableRealtimePublisher publisher = mock(TableRealtimePublisher.class);
        final GameRound round = new GameRound();
        final RoundPlayer a = player(1L, "A", 1);
        final RoundPlayer b = player(2L, "B", 2);
        final FriendlyTeenPattiService service;

        Fixture() {
            GameTable table = new GameTable(); table.setTableId(99L); table.setEntryBet(100L); round.setRoundId(1L); round.setGameTable(table);
            round.setRoundStatus(GameRoundStatus.IN_PROGRESS); round.setPhase(GamePhase.BETTING); round.setCurrentBet(5L); round.setPot(0L); round.setCurrentTurnGamePlayer(a.getGamePlayer());
            a.setGameRound(round); b.setGameRound(round);
            when(rounds.findByRoundIdForUpdate(1L)).thenReturn(Optional.of(round));
            when(rounds.findByGameTable_TableIdAndRoundStatus(99L, GameRoundStatus.IN_PROGRESS)).thenReturn(Optional.of(round));
            when(roundPlayers.findByGameRound_RoundIdAndGamePlayer_Player_PlayerId(1L, 1L)).thenReturn(Optional.of(a));
            when(roundPlayers.findByGameRound_RoundIdAndGamePlayer_Player_PlayerId(1L, 2L)).thenReturn(Optional.of(b));
            when(roundPlayers.findByGameRound_RoundIdOrderByGamePlayer_SeatNumber(1L)).thenReturn(List.of(a, b));
            when(roundPlayers.findByGameRound_RoundIdAndRoundPlayerStatusOrderByGamePlayer_SeatNumber(1L, RoundPlayerStatus.ACTIVE)).thenReturn(List.of(a, b));
            when(sideShows.findTopByGameRound_RoundIdAndStatusOrderByCreatedAtDesc(1L, "PENDING")).thenReturn(Optional.empty());
            when(cards.findByGameRound_RoundIdAndGamePlayer_GamePlayerIdOrderByCardPosition(1L, 1L)).thenReturn(hand("A", "S", "K", "S", "Q", "S"));
            when(cards.findByGameRound_RoundIdAndGamePlayer_GamePlayerIdOrderByCardPosition(1L, 2L)).thenReturn(hand("2", "D", "4", "C", "7", "H"));
            service = new FriendlyTeenPattiService(mock(GameTableService.class), mock(GameTableRepository.class), mock(GamePlayerRepository.class), rounds, roundPlayers,
                    cards, mock(PlayerRepository.class), mock(WalletTransactionRepository.class), sideShows,
                    new TeenPattiHandEvaluator(), publisher);
        }
    }

    private static List<PlayerCard> hand(String... values) {
        return java.util.stream.IntStream.range(0, values.length / 2).mapToObj(index -> {
            PlayerCard card = new PlayerCard(); card.setCardRank(values[index * 2]); card.setCardSuit(values[index * 2 + 1]); card.setCardPosition((short) (index + 1)); return card;
        }).toList();
    }

    private static RoundPlayer player(Long id, String name, int seat) {
        Player player = new Player(); player.setPlayerId(id); player.setUsername(name); player.setCoinBalance(10_000L);
        GamePlayer gamePlayer = new GamePlayer(); gamePlayer.setGamePlayerId(id); gamePlayer.setPlayer(player); gamePlayer.setSeatNumber((byte) seat);
        RoundPlayer state = new RoundPlayer(); state.setRoundPlayerId(id); state.setGamePlayer(gamePlayer); state.setVisibilityStatus(VisibilityStatus.BLIND); state.setRoundPlayerStatus(RoundPlayerStatus.ACTIVE); state.setTotalContribution(0L);
        return state;
    }
}
