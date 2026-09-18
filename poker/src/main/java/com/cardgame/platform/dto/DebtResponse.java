package com.cardgame.platform.dto;
public record DebtResponse(Long debtId, Long lenderPlayerId, String lenderUsername, Long originalAmount, Long remainingAmount, String status) { }
