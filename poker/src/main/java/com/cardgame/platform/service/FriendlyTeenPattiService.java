package com.cardgame.platform.service;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.exception.ResourceNotFoundException;
import com.cardgame.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/** Server-authoritative Teen Patti engine. Clients never supply cards, balances, turn order, or outcomes. */
@Service
@RequiredArgsConstructor
public class FriendlyTeenPattiService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<String> RANKS = List.of("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A");
    private static final List<String> SUITS = List.of("D", "C", "H", "S");
    private static final int VISIBILITY_SECONDS = 30, TURN_SECONDS = 30;
    private static final int SAME_BET_LIMIT = 5, FINAL_BET_TURN_LIMIT = 5;

    private final GameTableService gameTableService;
    private final GameTableRepository gameTableRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameRoundRepository gameRoundRepository;
    private final RoundPlayerRepository roundPlayerRepository;
    private final PlayerCardRepository playerCardRepository;
    private final PlayerRepository playerRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SideShowRepository sideShowRepository;
    private final TeenPattiHandEvaluator handEvaluator;
    private final TableRealtimePublisher tableRealtimePublisher;

    @Transactional
    public RoundResponse startRound(Long tableId, StartRoundRequest request) {
        GameTable table = gameTableService.getTable(tableId);
        if (!table.getHostPlayer().getPlayerId().equals(request.hostPlayerId())) throw new GameRuleViolationException("Only the host can start a round.");
        if (table.getTableStatus() != GameTableStatus.OPEN) throw new GameRuleViolationException("Finish the current round before starting another one.");
        List<GamePlayer> participants = gamePlayerRepository.findByGameTable_TableIdAndPlayerStatusOrderBySeatNumber(tableId, GamePlayerStatus.JOINED);
        if (participants.size() < 2) throw new GameRuleViolationException("At least two players are needed to start.");

        GameRound round = new GameRound();
        round.setGameTable(table); round.setRoundNumber(gameRoundRepository.findTopByGameTable_TableIdOrderByRoundNumberDesc(tableId).map(r -> r.getRoundNumber() + 1).orElse(1));
        round.setRoundStatus(GameRoundStatus.IN_PROGRESS); round.setPhase(GamePhase.CHOOSING_VISIBILITY); round.setCurrentBet(table.getEntryBet());
        round.setPot(0L); round.setStartedAt(LocalDateTime.now()); round.setVisibilityDeadline(LocalDateTime.now().plusSeconds(VISIBILITY_SECONDS));
        round = gameRoundRepository.save(round);
        List<DeckCard> deck = newDeck(); Collections.shuffle(deck, RANDOM); List<PlayerCard> cards = new ArrayList<>(); List<RoundPlayer> states = new ArrayList<>(); int nextCard = 0;
        for (GamePlayer participant : participants) {
            RoundPlayer state = new RoundPlayer(); state.setGameRound(round); state.setGamePlayer(participant); states.add(state);
            for (short position = 1; position <= 3; position++) {
                DeckCard deckCard = deck.get(nextCard++); PlayerCard card = new PlayerCard(); card.setGameRound(round); card.setGamePlayer(participant);
                card.setCardRank(deckCard.rank()); card.setCardSuit(deckCard.suit()); card.setDeckNumber((byte) 1); card.setCardPosition(position); cards.add(card);
            }
        }
        roundPlayerRepository.saveAll(states); playerCardRepository.saveAll(cards); table.setTableStatus(GameTableStatus.IN_GAME); gameTableRepository.save(table);
        return publish(round);
    }

    @Transactional
    public RoundResponse chooseVisibility(Long roundId, Long playerId, ChooseVisibilityRequest request) {
        GameRound round = active(roundId);
        if (round.getPhase() != GamePhase.CHOOSING_VISIBILITY) throw new GameRuleViolationException("The Blind/Seen selection period is over.");
        RoundPlayer state = state(roundId, playerId);
        if (state.getVisibilityStatus() != VisibilityStatus.PENDING) throw new GameRuleViolationException("You already selected your card visibility.");
        state.setVisibilityStatus(visibility(request.choice())); state.setSelectedAt(LocalDateTime.now()); roundPlayerRepository.save(state);
        if (allSelected(round)) beginBetting(round);
        return publish(round);
    }

    @Transactional
    public RoundResponse seeCards(Long roundId, Long playerId) {
        GameRound round = active(roundId); RoundPlayer state = state(roundId, playerId);
        if (state.getVisibilityStatus() != VisibilityStatus.BLIND) throw new GameRuleViolationException("Only a Blind player can reveal cards later.");
        state.setVisibilityStatus(VisibilityStatus.SEEN); state.setSelectedAt(LocalDateTime.now()); roundPlayerRepository.save(state); return publish(round);
    }

    @Transactional(readOnly = true)
    public MyHandResponse getMyHand(Long roundId, Long playerId) {
        RoundPlayer state = state(roundId, playerId);
        if (state.getVisibilityStatus() != VisibilityStatus.SEEN) throw new GameRuleViolationException("Choose SEE CARDS before viewing your hand.");
        List<CardResponse> cards = cards(state).stream().map(this::card).toList();
        if (cards.size() != 3) throw new GameRuleViolationException("Your hand is unavailable.");
        return new MyHandResponse(roundId, playerId, cards);
    }

    @Transactional(readOnly = true)
    public RoundResponse getActiveRound(Long tableId) {
        return response(gameRoundRepository.findByGameTable_TableIdAndRoundStatus(tableId, GameRoundStatus.IN_PROGRESS).orElseThrow(() -> new ResourceNotFoundException("This table has no active round.")));
    }

    @Transactional
    public RoundResponse takeTurn(Long roundId, Long playerId, TurnActionRequest request) {
        GameRound round = active(roundId); RoundPlayer state = current(round, playerId); String action = request.action().trim().toUpperCase(Locale.ROOT);
        if ("DROP".equals(action)) { drop(round, state); return publish(round); }
        if (round.getPhase() != GamePhase.BETTING && round.getPhase() != GamePhase.FINAL_TWO) throw new GameRuleViolationException("A betting action is not available right now.");
        if ("SHOW".equals(action)) {
            if (showEligiblePlayers(round).size() != 2) throw new GameRuleViolationException("SHOW is available only when exactly two live players remain.");
            if (request.amount() == null || request.amount() <= round.getCurrentBet()) throw new GameRuleViolationException("SHOW requires a bet at least 1 coin above the current bet.");
            debit(round, state, request.amount(), "SHOW_BET");
            round.setCurrentBet(request.amount());
            if (round.getPhase() != GamePhase.FINAL_TWO) resetSameBetActions(round);
            finishCompared(round);
            return publish(round);
        }
        if (round.getPhase() == GamePhase.FINAL_TWO && state.getFinalBetTurns() >= FINAL_BET_TURN_LIMIT) throw new GameRuleViolationException("You have used all five final betting turns.");
        long amount;
        boolean increasedBet = false;
        if ("SAME".equals(action)) {
            if (round.getPhase() != GamePhase.FINAL_TWO && state.getSameBetActions() >= SAME_BET_LIMIT) throw new GameRuleViolationException("You have used all five SAME actions at this bet. Raise to continue.");
            amount = round.getCurrentBet();
        } else if ("RAISE".equals(action)) {
            if (request.amount() == null || request.amount() <= round.getCurrentBet()) throw new GameRuleViolationException("A raise must be at least 1 coin above the current bet.");
            amount = request.amount(); round.setCurrentBet(amount); increasedBet = true;
        }
        else throw new GameRuleViolationException("Allowed actions are SAME, RAISE, SHOW, and DROP.");
        debit(round, state, amount, action);
        if (round.getPhase() == GamePhase.FINAL_TWO) {
            state.setFinalBetTurns((byte) (state.getFinalBetTurns() + 1)); roundPlayerRepository.save(state);
            if (allFinalTurnsUsed(round)) { finishCompared(round); return publish(round); }
        } else if (increasedBet) {
            resetSameBetActions(round);
            enterFinalTwoIfEligible(round);
        } else {
            state.setSameBetActions((byte) (state.getSameBetActions() + 1)); roundPlayerRepository.saveAndFlush(state);
        }
        advance(round, state); return publish(round);
    }

    @Transactional
    public RoundResponse leaveRound(Long roundId, Long playerId) { GameRound round = active(roundId); RoundPlayer player = state(roundId, playerId); if (player.getRoundPlayerStatus() != RoundPlayerStatus.ACTIVE) throw new GameRuleViolationException("You already dropped."); drop(round, player); return publish(round); }

    @Transactional
    public RoundResponse requestSideShow(Long roundId, Long playerId, SideShowRequest request) {
        GameRound round = active(roundId); RoundPlayer requester = current(round, playerId);
        if (round.getPhase() != GamePhase.BETTING && round.getPhase() != GamePhase.FINAL_TWO) throw new GameRuleViolationException("A Side Show is not available right now.");
        if (round.getPhase() == GamePhase.FINAL_TWO && requester.getFinalBetTurns() >= FINAL_BET_TURN_LIMIT) throw new GameRuleViolationException("You have used all five final betting turns.");
        long previousBet = round.getCurrentBet();
        long requestedBet = request.amount();
        if (requestedBet <= previousBet) throw new GameRuleViolationException("A Side Show requires a bet higher than the current bet.");
        RoundPlayer target = nextActivePlayer(round, requester);
        debit(round, requester, requestedBet, "SIDE_SHOW_BET");
        round.setCurrentBet(requestedBet); gameRoundRepository.save(round);
        if (round.getPhase() == GamePhase.FINAL_TWO) {
            requester.setFinalBetTurns((byte) (requester.getFinalBetTurns() + 1)); roundPlayerRepository.save(requester);
        } else {
            resetSameBetActions(round);
            enterFinalTwoIfEligible(round);
        }
        SideShow show = new SideShow(); show.setGameRound(round); show.setRequesterGamePlayer(requester.getGamePlayer()); show.setRequestedGamePlayer(target.getGamePlayer()); sideShowRepository.save(show);
        round.setPhase(GamePhase.SIDE_SHOW_PENDING); round.setTurnDeadline(null); gameRoundRepository.save(round); return publish(round);
    }

    @Transactional
    public RoundResponse respondToSideShow(Long roundId, Long sideShowId, Long playerId, SideShowResponseRequest request) {
        GameRound round = active(roundId); if (round.getPhase() != GamePhase.SIDE_SHOW_PENDING) throw new GameRuleViolationException("There is no pending Side Show.");
        SideShow show = sideShowRepository.findBySideShowIdAndStatus(sideShowId, "PENDING").orElseThrow(() -> new ResourceNotFoundException("Side Show was not found."));
        if (!show.getGameRound().getRoundId().equals(roundId) || !show.getRequestedGamePlayer().getPlayer().getPlayerId().equals(playerId)) throw new GameRuleViolationException("Only the requested player can respond.");
        String answer = request.response().trim().toUpperCase(Locale.ROOT); if (!answer.equals("ACCEPT") && !answer.equals("REJECT")) throw new GameRuleViolationException("Respond ACCEPT or REJECT.");
        show.setStatus(answer); show.setRespondedAt(LocalDateTime.now()); sideShowRepository.save(show);
        RoundPlayer requester = byGamePlayer(roundId, show.getRequesterGamePlayer().getGamePlayerId()); RoundPlayer target = byGamePlayer(roundId, show.getRequestedGamePlayer().getGamePlayerId());
        if (answer.equals("ACCEPT")) { RoundPlayer weaker = compare(requester, target) >= 0 ? target : requester; weaker.setRoundPlayerStatus(RoundPlayerStatus.DROPPED); weaker.setDroppedAt(LocalDateTime.now()); roundPlayerRepository.save(weaker); continueOrFinish(round, requester); }
        else {
            if (Boolean.TRUE.equals(round.getFinalTwoStarted())) round.setPhase(GamePhase.FINAL_TWO);
            else enterFinalTwoIfEligible(round);
            if (round.getPhase() == GamePhase.FINAL_TWO && allFinalTurnsUsed(round)) finishCompared(round);
            else if (round.getPhase() == GamePhase.FINAL_TWO) advance(round, requester);
            else { round.setPhase(GamePhase.BETTING); round.setCurrentTurnGamePlayer(requester.getGamePlayer()); round.setTurnDeadline(LocalDateTime.now().plusSeconds(TURN_SECONDS)); gameRoundRepository.save(round); }
        }
        return publish(round);
    }

    @Transactional
    public void processTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        for (GameRound round : gameRoundRepository.findByRoundStatus(GameRoundStatus.IN_PROGRESS)) {
            if (round.getPhase() == GamePhase.CHOOSING_VISIBILITY && round.getVisibilityDeadline() != null && !round.getVisibilityDeadline().isAfter(now)) {
                roundPlayerRepository.findByGameRound_RoundIdOrderByGamePlayer_SeatNumber(round.getRoundId()).stream().filter(p -> p.getVisibilityStatus() == VisibilityStatus.PENDING).forEach(p -> { p.setVisibilityStatus(VisibilityStatus.BLIND); p.setSelectedAt(now); }); beginBetting(round); publish(round);
            } else if ((round.getPhase() == GamePhase.BETTING || round.getPhase() == GamePhase.FINAL_TWO) && round.getTurnDeadline() != null && !round.getTurnDeadline().isAfter(now)) {
                drop(round, byGamePlayer(round.getRoundId(), round.getCurrentTurnGamePlayer().getGamePlayerId())); publish(round);
            }
        }
    }

    private void beginBetting(GameRound round) { List<RoundPlayer> players = activePlayers(round); RoundPlayer first = players.get(RANDOM.nextInt(players.size())); round.setPhase(GamePhase.BETTING); round.setCurrentTurnGamePlayer(first.getGamePlayer()); round.setVisibilityDeadline(null); round.setTurnDeadline(LocalDateTime.now().plusSeconds(TURN_SECONDS)); gameRoundRepository.save(round); }
    private void debit(GameRound round, RoundPlayer state, long amount, String type) { Player player = state.getGamePlayer().getPlayer(); if (player.getCoinBalance() < amount) throw new GameRuleViolationException("Insufficient virtual coins. Drop or request a loan."); player.setCoinBalance(player.getCoinBalance() - amount); playerRepository.save(player); WalletTransaction tx = new WalletTransaction(); tx.setPlayer(player); tx.setGameRound(round); tx.setTransactionType(type); tx.setAmount(-amount); tx.setBalanceAfter(player.getCoinBalance()); walletTransactionRepository.save(tx); state.setTotalContribution(state.getTotalContribution() + amount); roundPlayerRepository.save(state); round.setPot(round.getPot() + amount); gameRoundRepository.save(round); }
    private void drop(GameRound round, RoundPlayer player) { player.setRoundPlayerStatus(RoundPlayerStatus.DROPPED); player.setDroppedAt(LocalDateTime.now()); roundPlayerRepository.save(player); continueOrFinish(round, player); }
    private void continueOrFinish(GameRound round, RoundPlayer previous) {
        List<RoundPlayer> left = activePlayers(round);
        if (left.size() == 1) { finish(round, left.getFirst(), false); return; }
        enterFinalTwoIfEligible(round);
        if (round.getPhase() != GamePhase.FINAL_TWO) round.setPhase(GamePhase.BETTING);
        advance(round, previous);
    }
    private void advance(GameRound round, RoundPlayer previous) {
        List<RoundPlayer> players = activePlayers(round);
        if (round.getPhase() == GamePhase.FINAL_TWO) {
            players = players.stream().filter(player -> player.getFinalBetTurns() < FINAL_BET_TURN_LIMIT).toList();
            if (players.isEmpty()) { finishCompared(round); return; }
        }
        int previousSeat = previous.getGamePlayer().getSeatNumber();
        RoundPlayer next = players.stream().filter(player -> player.getGamePlayer().getSeatNumber() > previousSeat).findFirst().orElse(players.getFirst());
        round.setCurrentTurnGamePlayer(next.getGamePlayer()); round.setTurnDeadline(LocalDateTime.now().plusSeconds(TURN_SECONDS)); gameRoundRepository.save(round);
    }
    private void resetSameBetActions(GameRound round) {
        List<RoundPlayer> players = activePlayers(round);
        players.forEach(player -> player.setSameBetActions((byte) 0));
        roundPlayerRepository.saveAllAndFlush(players);
    }
    private void enterFinalTwoIfEligible(GameRound round) {
        List<RoundPlayer> players = activePlayers(round);
        if (players.size() != 2 || round.getCurrentBet() < finalTwoThreshold(round)) return;
        if (!Boolean.TRUE.equals(round.getFinalTwoStarted())) {
            players.forEach(player -> player.setFinalBetTurns((byte) 0));
            roundPlayerRepository.saveAll(players);
            round.setFinalTwoStarted(true);
        }
        if (round.getPhase() != GamePhase.FINAL_TWO) {
            round.setPhase(GamePhase.FINAL_TWO);
        }
        gameRoundRepository.save(round);
    }
    private boolean allFinalTurnsUsed(GameRound round) { return activePlayers(round).stream().allMatch(player -> player.getFinalBetTurns() >= FINAL_BET_TURN_LIMIT); }
    private long finalTwoThreshold(GameRound round) { return Math.multiplyExact(round.getGameTable().getEntryBet(), 15L); }
    private void finishCompared(GameRound round) { finish(round, activePlayers(round).stream().max(this::compare).orElseThrow(), true); }
    private void finish(GameRound round, RoundPlayer winner, boolean revealCards) { Player player = winner.getGamePlayer().getPlayer(); player.setCoinBalance(player.getCoinBalance() + round.getPot()); playerRepository.save(player); WalletTransaction tx = new WalletTransaction(); tx.setPlayer(player); tx.setGameRound(round); tx.setTransactionType("POT_WIN"); tx.setAmount(round.getPot()); tx.setBalanceAfter(player.getCoinBalance()); walletTransactionRepository.save(tx); round.setWinnerPlayer(player); round.setRoundStatus(GameRoundStatus.COMPLETED); round.setPhase(GamePhase.COMPLETED); round.setEndedAt(LocalDateTime.now()); round.setCurrentTurnGamePlayer(null); round.setTurnDeadline(null); gameRoundRepository.save(round); GameTable table = round.getGameTable(); table.setTableStatus(GameTableStatus.OPEN); gameTableRepository.save(table); tableRealtimePublisher.showdown(response(round), new ShowdownResponse(round.getRoundId(), player.getPlayerId(), player.getUsername(), revealCards ? "SHOWDOWN" : "LAST_PLAYER_STANDING", revealCards ? publicHands(round) : List.of())); }
    private int compare(RoundPlayer a, RoundPlayer b) { return handEvaluator.evaluate(cards(a)).compareTo(handEvaluator.evaluate(cards(b))); }
    private List<PlayerCard> cards(RoundPlayer p) { return playerCardRepository.findByGameRound_RoundIdAndGamePlayer_GamePlayerIdOrderByCardPosition(p.getGameRound().getRoundId(), p.getGamePlayer().getGamePlayerId()); }
    private List<PlayerHandResult> publicHands(GameRound r) { return activePlayers(r).stream().map(p -> new PlayerHandResult(p.getGamePlayer().getPlayer().getPlayerId(), p.getGamePlayer().getPlayer().getUsername(), handEvaluator.evaluate(cards(p)).category().name(), cards(p).stream().map(this::card).toList())).toList(); }
    private List<RoundPlayer> activePlayers(GameRound r) { return roundPlayerRepository.findByGameRound_RoundIdAndRoundPlayerStatusOrderByGamePlayer_SeatNumber(r.getRoundId(), RoundPlayerStatus.ACTIVE); }
    private List<RoundPlayer> showEligiblePlayers(GameRound round) { return activePlayers(round).stream().filter(player -> player.getGamePlayer().getPlayerStatus() == GamePlayerStatus.JOINED).toList(); }
    private RoundPlayer nextActivePlayer(GameRound round, RoundPlayer requester) {
        List<RoundPlayer> players = activePlayers(round);
        int requesterIndex = players.stream().map(RoundPlayer::getRoundPlayerId).toList().indexOf(requester.getRoundPlayerId());
        if (requesterIndex < 0 || players.size() < 2) throw new GameRuleViolationException("No active player is available for a Side Show.");
        return players.get((requesterIndex + 1) % players.size());
    }
    private boolean allSelected(GameRound r) { return roundPlayerRepository.findByGameRound_RoundIdOrderByGamePlayer_SeatNumber(r.getRoundId()).stream().noneMatch(p -> p.getVisibilityStatus() == VisibilityStatus.PENDING); }
    private RoundPlayer current(GameRound r, Long playerId) { if (r.getCurrentTurnGamePlayer() == null || !r.getCurrentTurnGamePlayer().getPlayer().getPlayerId().equals(playerId)) throw new GameRuleViolationException("It is not your turn."); return state(r.getRoundId(), playerId); }
    private RoundPlayer state(Long roundId, Long playerId) { return roundPlayerRepository.findByGameRound_RoundIdAndGamePlayer_Player_PlayerId(roundId, playerId).orElseThrow(() -> new ResourceNotFoundException("You are not in this round.")); }
    private RoundPlayer byGamePlayer(Long roundId, Long id) { return roundPlayerRepository.findByGameRound_RoundIdAndGamePlayer_GamePlayerId(roundId, id).orElseThrow(); }
    private GameRound active(Long id) { GameRound round = gameRoundRepository.findByRoundIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Round was not found.")); if (round.getRoundStatus() != GameRoundStatus.IN_PROGRESS) throw new GameRuleViolationException("This round has ended."); return round; }
    private VisibilityStatus visibility(String value) { try { VisibilityStatus result = VisibilityStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)); if (result == VisibilityStatus.PENDING) throw new IllegalArgumentException(); return result; } catch (IllegalArgumentException e) { throw new GameRuleViolationException("Choose BLIND or SEEN."); } }
    private CardResponse card(PlayerCard c) { return new CardResponse(c.getCardRank(), c.getCardSuit(), c.getCardPosition().intValue()); }
    private RoundResponse publish(GameRound r) { RoundResponse response = response(r); if (r.getRoundStatus() == GameRoundStatus.IN_PROGRESS) tableRealtimePublisher.roundStarted(response); return response; }
    private RoundResponse response(GameRound r) { List<RoundPlayerResponse> players = roundPlayerRepository.findByGameRound_RoundIdOrderByGamePlayer_SeatNumber(r.getRoundId()).stream().map(p -> new RoundPlayerResponse(p.getGamePlayer().getPlayer().getPlayerId(), p.getGamePlayer().getPlayer().getUsername(), p.getGamePlayer().getSeatNumber().intValue(), p.getVisibilityStatus().name(), p.getRoundPlayerStatus().name(), p.getTotalContribution(), p.getSameBetActions().intValue(), p.getFinalBetTurns().intValue(), p.getGamePlayer().getPlayerStatus() == GamePlayerStatus.JOINED)).toList(); SideShow pending = sideShowRepository.findTopByGameRound_RoundIdAndStatusOrderByCreatedAtDesc(r.getRoundId(), "PENDING").orElse(null); return new RoundResponse(r.getRoundId(), r.getGameTable().getTableId(), r.getRoundNumber(), r.getRoundStatus().name(), r.getPhase().name(), r.getCurrentBet(), r.getPot(), r.getCurrentTurnGamePlayer() == null ? null : r.getCurrentTurnGamePlayer().getPlayer().getPlayerId(), r.getVisibilityDeadline(), r.getTurnDeadline(), finalTwoThreshold(r), FINAL_BET_TURN_LIMIT, pending == null ? null : pending.getSideShowId(), pending == null ? null : pending.getRequesterGamePlayer().getPlayer().getPlayerId(), pending == null ? null : pending.getRequestedGamePlayer().getPlayer().getPlayerId(), players); }
    private List<DeckCard> newDeck() { List<DeckCard> deck = new ArrayList<>(52); for (String suit : SUITS) for (String rank : RANKS) deck.add(new DeckCard(rank, suit)); return deck; }
    private record DeckCard(String rank, String suit) { }
}
