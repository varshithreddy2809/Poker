-- Phase 4: server-authoritative admin roles and virtual-coin audit trail.
-- Run once after phases 1-3. This changes schema only; it creates no admin.
USE card_game_platform;

ALTER TABLE players
    ADD COLUMN account_role VARCHAR(20) NOT NULL DEFAULT 'PLAYER' AFTER account_status,
    ADD CONSTRAINT chk_players_account_role CHECK (account_role IN ('PLAYER', 'ADMIN'));

CREATE TABLE admin_coin_transactions (
    transaction_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    admin_id BIGINT UNSIGNED NOT NULL,
    target_player_id BIGINT UNSIGNED NOT NULL,
    table_id BIGINT UNSIGNED NULL,
    operation_id VARCHAR(80) NOT NULL,
    operation_type VARCHAR(30) NOT NULL,
    amount BIGINT UNSIGNED NOT NULL,
    reason VARCHAR(250) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_admin_coin_transactions_admin FOREIGN KEY (admin_id) REFERENCES players (player_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_admin_coin_transactions_target FOREIGN KEY (target_player_id) REFERENCES players (player_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_admin_coin_transactions_table FOREIGN KEY (table_id) REFERENCES game_tables (table_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT uk_admin_coin_operation_target UNIQUE (operation_id, target_player_id),
    INDEX idx_admin_coin_transactions_created (created_at),
    INDEX idx_admin_coin_transactions_target (target_player_id, created_at)
) ENGINE = InnoDB;
