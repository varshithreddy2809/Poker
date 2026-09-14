-- Phase 1: generic card-game platform foundation
-- Target: MySQL 8.0+
-- This migration deliberately contains no betting, wallet, debt, action,
-- side-show, result, spectator, notification, or voice-chat tables.

CREATE DATABASE IF NOT EXISTS card_game_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE card_game_platform;

CREATE TABLE IF NOT EXISTS players (
    player_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    coin_balance BIGINT UNSIGNED NOT NULL DEFAULT 0,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id),
    CONSTRAINT uk_players_username UNIQUE (username),
    CONSTRAINT uk_players_email UNIQUE (email),
    CONSTRAINT chk_players_account_status
        CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED')),
    INDEX idx_players_account_status (account_status),
    INDEX idx_players_created_at (created_at)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS game_types (
    game_type_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    min_players TINYINT UNSIGNED NOT NULL,
    max_players TINYINT UNSIGNED NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    rules_config JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (game_type_id),
    CONSTRAINT uk_game_types_code UNIQUE (code),
    CONSTRAINT chk_game_types_player_limits
        CHECK (min_players >= 1 AND max_players >= min_players),
    INDEX idx_game_types_active (is_active)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS game_tables (
    table_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    game_type_id SMALLINT UNSIGNED NOT NULL,
    host_player_id BIGINT UNSIGNED NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    entry_bet BIGINT UNSIGNED NOT NULL,
    max_players TINYINT UNSIGNED NOT NULL,
    table_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (table_id),
    CONSTRAINT fk_game_tables_game_type
        FOREIGN KEY (game_type_id) REFERENCES game_types (game_type_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_game_tables_host
        FOREIGN KEY (host_player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_game_tables_max_players CHECK (max_players >= 2),
    CONSTRAINT chk_game_tables_status
        CHECK (table_status IN ('OPEN', 'IN_GAME', 'CLOSED')),
    INDEX idx_game_tables_game_type_status (game_type_id, table_status),
    INDEX idx_game_tables_host (host_player_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS game_players (
    game_player_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    table_id BIGINT UNSIGNED NOT NULL,
    player_id BIGINT UNSIGNED NOT NULL,
    seat_number TINYINT UNSIGNED NOT NULL,
    player_status VARCHAR(20) NOT NULL DEFAULT 'JOINED',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    PRIMARY KEY (game_player_id),
    CONSTRAINT uk_game_players_table_player UNIQUE (table_id, player_id),
    CONSTRAINT uk_game_players_table_seat UNIQUE (table_id, seat_number),
    CONSTRAINT fk_game_players_table
        FOREIGN KEY (table_id) REFERENCES game_tables (table_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_game_players_player
        FOREIGN KEY (player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_game_players_seat_number CHECK (seat_number >= 1),
    CONSTRAINT chk_game_players_status
        CHECK (player_status IN ('JOINED', 'LEFT', 'DISCONNECTED')),
    INDEX idx_game_players_table_status (table_id, player_status),
    INDEX idx_game_players_player (player_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS game_rounds (
    round_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    table_id BIGINT UNSIGNED NOT NULL,
    round_number INT UNSIGNED NOT NULL,
    round_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    winner_player_id BIGINT UNSIGNED NULL,
    started_at TIMESTAMP NULL,
    ended_at TIMESTAMP NULL,
    PRIMARY KEY (round_id),
    CONSTRAINT uk_game_rounds_table_round UNIQUE (table_id, round_number),
    CONSTRAINT fk_game_rounds_table
        FOREIGN KEY (table_id) REFERENCES game_tables (table_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_game_rounds_winner
        FOREIGN KEY (winner_player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_game_rounds_number CHECK (round_number >= 1),
    CONSTRAINT chk_game_rounds_status
        CHECK (round_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    INDEX idx_game_rounds_table_status (table_id, round_status),
    INDEX idx_game_rounds_winner (winner_player_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS player_cards (
    player_card_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    round_id BIGINT UNSIGNED NOT NULL,
    game_player_id BIGINT UNSIGNED NOT NULL,
    card_rank VARCHAR(5) NOT NULL,
    card_suit VARCHAR(20) NOT NULL,
    deck_number TINYINT UNSIGNED NOT NULL DEFAULT 1,
    card_position SMALLINT UNSIGNED NOT NULL,
    dealt_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (player_card_id),
    CONSTRAINT uk_player_cards_player_position
        UNIQUE (round_id, game_player_id, card_position),
    CONSTRAINT uk_player_cards_unique_deck_card
        UNIQUE (round_id, deck_number, card_rank, card_suit),
    CONSTRAINT fk_player_cards_round
        FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_player_cards_game_player
        FOREIGN KEY (game_player_id) REFERENCES game_players (game_player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_player_cards_deck_number CHECK (deck_number >= 1),
    CONSTRAINT chk_player_cards_position CHECK (card_position >= 1),
    INDEX idx_player_cards_round_player (round_id, game_player_id)
) ENGINE = InnoDB;

-- Initial catalog entry for the first supported game.
INSERT INTO game_types
    (code, display_name, min_players, max_players, is_active, rules_config)
VALUES
    ('TEEN_PATTI', 'Teen Patti', 2, 8, TRUE,
     JSON_OBJECT('cardsPerPlayer', 3, 'deckCount', 1))
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    min_players = VALUES(min_players),
    max_players = VALUES(max_players),
    is_active = VALUES(is_active),
    rules_config = VALUES(rules_config);
