package com.cardgame.platform.service;

import com.cardgame.platform.entity.PlayerCard;
import org.springframework.stereotype.Component;

import java.util.*;

/** Server-side Teen Patti hand ranking. Cards never come from a client request. */
@Component
public class TeenPattiHandEvaluator {
    private static final Map<String, Integer> RANKS = Map.ofEntries(
            Map.entry("2", 2), Map.entry("3", 3), Map.entry("4", 4), Map.entry("5", 5),
            Map.entry("6", 6), Map.entry("7", 7), Map.entry("8", 8), Map.entry("9", 9),
            Map.entry("T", 10), Map.entry("J", 11), Map.entry("Q", 12), Map.entry("K", 13), Map.entry("A", 14));
    private static final Map<String, Integer> SUITS = Map.of("D", 1, "C", 2, "H", 3, "S", 4);

    public HandValue evaluate(List<PlayerCard> cards) {
        if (cards.size() != 3) {
            throw new IllegalArgumentException("Teen Patti requires exactly three cards per hand.");
        }
        List<CardValue> values = cards.stream().map(this::toCardValue)
                .sorted(Comparator.comparingInt(CardValue::rank).reversed().thenComparing(Comparator.comparingInt(CardValue::suit).reversed()))
                .toList();
        Map<Integer, Long> counts = values.stream().collect(java.util.stream.Collectors.groupingBy(CardValue::rank, java.util.stream.Collectors.counting()));
        boolean color = values.stream().map(CardValue::suit).distinct().count() == 1;
        int sequenceHigh = sequenceHigh(values.stream().map(CardValue::rank).toList());
        boolean sequence = sequenceHigh > 0;

        HandCategory category;
        List<Integer> rankTieBreak;
        if (counts.size() == 1) {
            category = HandCategory.TRAIL;
            rankTieBreak = List.of(values.getFirst().rank());
        } else if (sequence && color) {
            category = HandCategory.PURE_SEQUENCE;
            rankTieBreak = List.of(sequenceHigh);
        } else if (sequence) {
            category = HandCategory.SEQUENCE;
            rankTieBreak = List.of(sequenceHigh);
        } else if (color) {
            category = HandCategory.COLOR;
            rankTieBreak = values.stream().map(CardValue::rank).toList();
        } else if (counts.size() == 2) {
            category = HandCategory.PAIR;
            int pair = counts.entrySet().stream().filter(entry -> entry.getValue() == 2).findFirst().orElseThrow().getKey();
            int kicker = counts.entrySet().stream().filter(entry -> entry.getValue() == 1).findFirst().orElseThrow().getKey();
            rankTieBreak = List.of(pair, kicker);
        } else {
            category = HandCategory.HIGH_CARD;
            rankTieBreak = values.stream().map(CardValue::rank).toList();
        }
        return new HandValue(category, rankTieBreak, values.stream().map(CardValue::suit).toList());
    }

    private CardValue toCardValue(PlayerCard card) {
        Integer rank = RANKS.get(card.getCardRank());
        Integer suit = SUITS.get(card.getCardSuit());
        if (rank == null || suit == null) {
            throw new IllegalArgumentException("Only a standard Teen Patti deck can be evaluated.");
        }
        return new CardValue(rank, suit);
    }

    private int sequenceHigh(List<Integer> ranks) {
        List<Integer> ordered = ranks.stream().sorted().toList();
        if (ordered.equals(List.of(2, 3, 14))) {
            return 3; // A-2-3 is the lowest valid sequence.
        }
        return ordered.get(0) + 1 == ordered.get(1) && ordered.get(1) + 1 == ordered.get(2) ? ordered.get(2) : -1;
    }

    private record CardValue(int rank, int suit) {
    }

    public record HandValue(HandCategory category, List<Integer> rankTieBreak, List<Integer> suitTieBreak)
            implements Comparable<HandValue> {
        @Override
        public int compareTo(HandValue other) {
            int comparison = Integer.compare(category.strength(), other.category.strength());
            if (comparison != 0) return comparison;
            comparison = compareLists(rankTieBreak, other.rankTieBreak);
            return comparison != 0 ? comparison : compareLists(suitTieBreak, other.suitTieBreak);
        }

        private static int compareLists(List<Integer> first, List<Integer> second) {
            for (int index = 0; index < Math.min(first.size(), second.size()); index++) {
                int comparison = Integer.compare(first.get(index), second.get(index));
                if (comparison != 0) return comparison;
            }
            return Integer.compare(first.size(), second.size());
        }
    }
}
