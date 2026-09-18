package com.cardgame.platform.dto;
import java.util.List;
public record BorrowingStateResponse(List<CoinBorrowRequestResponse> pendingIncoming, List<CoinBorrowRequestResponse> pendingOutgoing, List<DebtResponse> outstandingDebts) { }
