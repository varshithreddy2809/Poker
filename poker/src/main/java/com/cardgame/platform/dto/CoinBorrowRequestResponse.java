package com.cardgame.platform.dto;
public record CoinBorrowRequestResponse(Long borrowRequestId, Long roundId, Long borrowerPlayerId, String borrowerUsername, Long lenderPlayerId, String lenderUsername, Long amount, String status) { }
