package com.cardgame.platform.service;

import com.cardgame.platform.entity.PlayerCard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeenPattiHandEvaluatorTest {
    private final TeenPattiHandEvaluator evaluator = new TeenPattiHandEvaluator();

    @Test
    void trailBeatsPureSequence() {
        var trail = evaluator.evaluate(hand("A", "S", "A", "H", "A", "D"));
        var pureSequence = evaluator.evaluate(hand("K", "S", "Q", "S", "J", "S"));

        assertEquals(HandCategory.TRAIL, trail.category());
        assertTrue(trail.compareTo(pureSequence) > 0);
    }

    @Test
    void aceTwoThreeIsAValidLowSequence() {
        var aceLow = evaluator.evaluate(hand("A", "S", "2", "H", "3", "D"));
        var highCard = evaluator.evaluate(hand("A", "H", "J", "D", "8", "S"));

        assertEquals(HandCategory.SEQUENCE, aceLow.category());
        assertTrue(aceLow.compareTo(highCard) > 0);
    }

    private List<PlayerCard> hand(String... values) {
        return java.util.stream.IntStream.range(0, values.length / 2).mapToObj(index -> {
            PlayerCard card = new PlayerCard();
            card.setCardRank(values[index * 2]);
            card.setCardSuit(values[index * 2 + 1]);
            card.setCardPosition((short) (index + 1));
            return card;
        }).toList();
    }
}
