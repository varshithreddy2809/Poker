package com.cardgame.platform.service;

import com.cardgame.platform.dto.*;
import com.cardgame.platform.entity.*;
import com.cardgame.platform.exception.GameRuleViolationException;
import com.cardgame.platform.exception.ResourceNotFoundException;
import com.cardgame.platform.repository.GamePlayerRepository;
import com.cardgame.platform.repository.GameRoundRepository;
import com.cardgame.platform.repository.GameTableRepository;
import com.cardgame.platform.repository.PlayerCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Friendly mode has no coins or betting. A host starts a round, the server
 * deals the cards, and the host starts a final showdown when everyone is ready.
 */
@Service
@RequiredArgsConstructor
public class FriendlyTeenPattiService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<String> RANKS = List.of("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A");
    private static final List<String> SUITS = List.of("D", "C", "H", "S");

    private final GameTableService gameTableService;
    private final GameTableRepository gameTableRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameRoundRepository gameRoundRepository;
    private final PlayerCardRepository playerCardRepository;
    private final TeenPattiHandEvaluator handEvaluator;

    @Transactional
    public RoundResponse startRound(Long tableId, StartRoundRequest request) {
        GameTable table = gameTableService.getTable(tableId);
        requireHost(table, request.hostPlayerId());
        if (table.getTableStatus() != GameTableStatus.OPEN) {
            throw new GameRuleViolationException("Finish the current round before starting another one.");
        }
        if (gameRoundRepository.existsByGameTable_TableIdAndRoundStatus(tableId, GameRoundStatus.IN_PROGRESS)) {
            throw new GameRuleViolationException("This table already has an active round.");
        }

        List<GamePlayer> participants = gamePlayerRepository
                .findByGameTable_TableIdAndPlayerStatusOrderBySeatNumber(tableId, GamePlayerStatus.JOINED);
        if (participants.size() < table.getGameType().getMinPlayers()) {
            throw new GameRuleViolationException("At least " + table.getGameType().getMinPlayers() + " players are needed to start.");
        }

        GameRound round = new GameRound();
        round.setGameTable(table);
        round.setRoundNumber(gameRoundRepository.findTopByGameTable_TableIdOrderByRoundNumberDesc(tableId)
                .map(previous -> previous.getRoundNumber() + 1).orElse(1));
        round.setRoundStatus(GameRoundStatus.IN_PROGRESS);
        round.setStartedAt(LocalDateTime.now());
        round = gameRoundRepository.save(round);

        List<DeckCard> deck = newDeck();
        Collections.shuffle(deck, RANDOM);
        List<PlayerCard> dealtCards = new ArrayList<>();
        int deckIndex = 0;
        for (GamePlayer participant : participants) {
            for (short position = 1; position <= 3; position++) {
                DeckCard deckCard = deck.get(deckIndex++);
                PlayerCard card = new PlayerCard();
                card.setGameRound(round);
                card.setGamePlayer(participant);
                card.setCardRank(deckCard.rank());
                card.setCardSuit(deckCard.suit());
                card.setDeckNumber((byte) 1);
                card.setCardPosition(position);
                dealtCards.add(card);
            }
        }
        playerCardRepository.saveAll(dealtCards);

        table.setTableStatus(GameTableStatus.IN_GAME);
        gameTableRepository.save(table);
        return new RoundResponse(round.getRoundId(), tableId, round.getRoundNumber(), round.getRoundStatus().name());
    }

    @Transactional(readOnly = true)
    public MyHandResponse getMyHand(Long roundId, Long playerId) {
        GameRound round = getRound(roundId);
        GamePlayer gamePlayer = gamePlayerRepository.findByGameTable_TableIdAndPlayer_PlayerId(
                        round.getGameTable().getTableId(), playerId)
                .orElseThrow(() -> new ResourceNotFoundException("This player is not seated at the round's table."));
        List<CardResponse> cards = playerCardRepository
                .findByGameRound_RoundIdAndGamePlayer_GamePlayerIdOrderByCardPosition(roundId, gamePlayer.getGamePlayerId())
                .stream().map(this::toCardResponse).toList();
        if (cards.size() != 3) {
            throw new GameRuleViolationException("The hand is not available for this player.");
        }
        return new MyHandResponse(roundId, playerId, cards);
    }

    @Transactional
    public ShowdownResponse showdown(Long roundId, ShowdownRequest request) {
        GameRound round = getRound(roundId);
        if (round.getRoundStatus() != GameRoundStatus.IN_PROGRESS) {
            throw new GameRuleViolationException("Only an active round can be shown down.");
        }
        GameTable table = round.getGameTable();
        requireHost(table, request.hostPlayerId());

        List<GamePlayer> participants = gamePlayerRepository
                .findByGameTable_TableIdAndPlayerStatusOrderBySeatNumber(table.getTableId(), GamePlayerStatus.JOINED);
        Map<Long, List<PlayerCard>> cardsByGamePlayer = playerCardRepository.findByGameRound_RoundIdOrderByGamePlayer_SeatNumberAscCardPositionAsc(roundId)
                .stream().collect(Collectors.groupingBy(card -> card.getGamePlayer().getGamePlayerId(), LinkedHashMap::new, Collectors.toList()));

        List<ResolvedHand> hands = participants.stream()
                .map(participant -> new ResolvedHand(participant, cardsByGamePlayer.getOrDefault(participant.getGamePlayerId(), List.of())))
                .peek(hand -> {
                    if (hand.cards().size() != 3) throw new GameRuleViolationException("Every active player must have three cards.");
                })
                .map(hand -> hand.withValue(handEvaluator.evaluate(hand.cards())))
                .toList();
        ResolvedHand winner = hands.stream().max(Comparator.comparing(ResolvedHand::value))
                .orElseThrow(() -> new GameRuleViolationException("There are no players to evaluate."));

        round.setWinnerPlayer(winner.gamePlayer().getPlayer());
        round.setRoundStatus(GameRoundStatus.COMPLETED);
        round.setEndedAt(LocalDateTime.now());
        table.setTableStatus(GameTableStatus.OPEN);
        gameRoundRepository.save(round);
        gameTableRepository.save(table);

        List<PlayerHandResult> resultHands = hands.stream().map(hand -> new PlayerHandResult(
                hand.gamePlayer().getPlayer().getPlayerId(), hand.gamePlayer().getPlayer().getUsername(),
                hand.value().category().name(), hand.cards().stream().map(this::toCardResponse).toList())).toList();
        return new ShowdownResponse(roundId, winner.gamePlayer().getPlayer().getPlayerId(),
                winner.gamePlayer().getPlayer().getUsername(), winner.value().category().name(), resultHands);
    }

    private GameRound getRound(Long roundId) {
        return gameRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round " + roundId + " was not found."));
    }

    private void requireHost(GameTable table, Long playerId) {
        if (!table.getHostPlayer().getPlayerId().equals(playerId)) {
            throw new GameRuleViolationException("Only the table host can perform this action.");
        }
    }

    private List<DeckCard> newDeck() {
        List<DeckCard> deck = new ArrayList<>(52);
        for (String suit : SUITS) for (String rank : RANKS) deck.add(new DeckCard(rank, suit));
        return deck;
    }

    private CardResponse toCardResponse(PlayerCard card) {
        return new CardResponse(card.getCardRank(), card.getCardSuit(), card.getCardPosition().intValue());
    }

    private record DeckCard(String rank, String suit) {
    }

    private record ResolvedHand(GamePlayer gamePlayer, List<PlayerCard> cards, TeenPattiHandEvaluator.HandValue value) {
        ResolvedHand(GamePlayer gamePlayer, List<PlayerCard> cards) {
            this(gamePlayer, cards, null);
        }

        ResolvedHand withValue(TeenPattiHandEvaluator.HandValue evaluatedValue) {
            return new ResolvedHand(gamePlayer, cards, evaluatedValue);
        }
    }
}
