-- Phase 3: virtual-coin borrow requests. Run once after phase-2-teen-patti-rules.sql.
USE card_game_platform;

ALTER TABLE round_players
    ADD COLUMN connection_status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED' AFTER total_contribution,
    ADD COLUMN disconnected_at DATETIME NULL AFTER connection_status,
    ADD COLUMN reconnect_deadline DATETIME NULL AFTER disconnected_at,
    ADD INDEX idx_round_players_reconnect (connection_status, reconnect_deadline);

CREATE TABLE IF NOT EXISTS coin_borrow_requests (
    borrow_request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    round_id BIGINT UNSIGNED NOT NULL,
    borrower_player_id BIGINT UNSIGNED NOT NULL,
    lender_player_id BIGINT UNSIGNED NOT NULL,
    amount BIGINT UNSIGNED NOT NULL,
    request_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at DATETIME NULL,
    PRIMARY KEY (borrow_request_id),
    CONSTRAINT fk_borrow_requests_round FOREIGN KEY (round_id) REFERENCES game_rounds (round_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_borrow_requests_borrower FOREIGN KEY (borrower_player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_borrow_requests_lender FOREIGN KEY (lender_player_id) REFERENCES players (player_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_borrow_requests_different_players CHECK (borrower_player_id <> lender_player_id),
    INDEX idx_borrow_requests_lender_status (lender_player_id, request_status),
    INDEX idx_borrow_requests_borrower_status (borrower_player_id, request_status)
) ENGINE = InnoDB;
