package com.cardgame.platform.dto;

import java.time.LocalDateTime;

public record AdminCoinTransactionResponse(Long transactionId, Long adminId, String adminUsername,
                                           Long targetPlayerId, String targetUsername, Long tableId,
                                           String operationType, Long amount, String reason,
                                           LocalDateTime createdAt) { }
