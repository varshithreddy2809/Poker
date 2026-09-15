-- Phase 2: authoritative Teen Patti round engine (virtual coins only).
-- Run once after phase-1-schema.sql on MySQL 8.0.29+.
USE card_game_platform;

ALTER TABLE players
    MODIFY coin_balance BIGINT UNSIGNED NOT NULL DEFAULT 10000;

ALTER TABLE game_rounds
    ADD COLUMN phase VARCHAR(30) NOT NULL DEFAULT 'CHOOSING_VISIBILITY' AFTER round_status,
    ADD COLUMN current_bet BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER phase,
    ADD COLUMN pot BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER current_bet,
    ADD COLUMN current_turn_game_player_id BIGINT UNSIGNED NULL AFTER pot,
    ADD COLUMN visibility_deadline DATETIME NULL AFTER current_turn_game_player_id,
    ADD COLUMN turn_deadline DATETIME NULL AFTER visibility_deadline,
    ADD COLUMN forced_same_turns_remaining TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER turn_deadline,
    ADD CONSTRAINT fk_game_rounds_current_turn
        FOREIGN KEY (current_turn_game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

CREATE TABLE IF NOT EXISTS round_players (
    round_player_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    round_id BIGINT UNSIGNED NOT NULL,
    game_player_id BIGINT UNSIGNED NOT NULL,
    visibility_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    round_player_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_contribution BIGINT UNSIGNED NOT NULL DEFAULT 0,
    selected_at DATETIME NULL,
    dropped_at DATETIME NULL,
    PRIMARY KEY (round_player_id),
    CONSTRAINT uk_round_players_round_game_player UNIQUE (round_id, game_player_id),
    CONSTRAINT fk_round_players_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_round_players_game_player FOREIGN KEY (game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_round_players_visibility CHECK (visibility_status IN ('PENDING', 'BLIND', 'SEEN')),
    CONSTRAINT chk_round_players_status CHECK (round_player_status IN ('ACTIVE', 'DROPPED')),
    INDEX idx_round_players_active (round_id, round_player_status)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS bets (
    bet_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    round_id BIGINT UNSIGNED NOT NULL,
    game_player_id BIGINT UNSIGNED NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    amount BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (bet_id),
    CONSTRAINT fk_bets_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_bets_game_player FOREIGN KEY (game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_bets_round (round_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS game_actions (
    game_action_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    round_id BIGINT UNSIGNED NOT NULL,
    game_player_id BIGINT UNSIGNED NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    details JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (game_action_id),
    CONSTRAINT fk_game_actions_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_game_actions_game_player FOREIGN KEY (game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_game_actions_round (round_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS side_shows (
    side_show_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    round_id BIGINT UNSIGNED NOT NULL,
    requester_game_player_id BIGINT UNSIGNED NOT NULL,
    requested_game_player_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at DATETIME NULL,
    PRIMARY KEY (side_show_id),
    CONSTRAINT fk_side_shows_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_side_shows_requester FOREIGN KEY (requester_game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_side_shows_requested FOREIGN KEY (requested_game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_side_shows_round_status (round_id, status)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS wallet_transactions (
    wallet_transaction_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    player_id BIGINT UNSIGNED NOT NULL,
    round_id BIGINT UNSIGNED NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount BIGINT NOT NULL,
    balance_after BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (wallet_transaction_id),
    CONSTRAINT fk_wallet_transactions_player FOREIGN KEY (player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_wallet_transactions_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_wallet_transactions_player (player_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS player_debts (
    debt_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    borrower_player_id BIGINT UNSIGNED NOT NULL,
    lender_player_id BIGINT UNSIGNED NOT NULL,
    original_amount BIGINT UNSIGNED NOT NULL,
    remaining_amount BIGINT UNSIGNED NOT NULL,
    debt_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at DATETIME NULL,
    PRIMARY KEY (debt_id),
    CONSTRAINT fk_player_debts_borrower FOREIGN KEY (borrower_player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_player_debts_lender FOREIGN KEY (lender_player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_player_debts_different_players CHECK (borrower_player_id <> lender_player_id),
    INDEX idx_player_debts_borrower_status (borrower_player_id, debt_status)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS debt_transactions (
    debt_transaction_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    debt_id BIGINT UNSIGNED NOT NULL,
    round_id BIGINT UNSIGNED NULL,
    amount BIGINT UNSIGNED NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (debt_transaction_id),
    CONSTRAINT fk_debt_transactions_debt FOREIGN KEY (debt_id) REFERENCES player_debts (debt_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_debt_transactions_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_debt_transactions_debt (debt_id, created_at)
) ENGINE = InnoDB;
