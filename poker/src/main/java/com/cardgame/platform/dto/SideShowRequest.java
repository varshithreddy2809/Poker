package com.cardgame.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** The proposed table bet. The server selects the Side Show target. */
public record SideShowRequest(@NotNull @Positive Long amount) {
}
