package com.cardgame.platform.service;

public enum HandCategory {
    HIGH_CARD(1), PAIR(2), COLOR(3), SEQUENCE(4), PURE_SEQUENCE(5), TRAIL(6);

    private final int strength;

    HandCategory(int strength) {
        this.strength = strength;
    }

    public int strength() {
        return strength;
    }
}
