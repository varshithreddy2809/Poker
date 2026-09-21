-- Phase 3: public Blind/Seen state and authoritative per-player betting limits.
-- Safe to run after phase-2 on both new and existing MySQL 8.0 installations.
USE card_game_platform;

SET @schema_name = DATABASE();

SET @migration_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE round_players ADD COLUMN same_bet_actions TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER total_contribution',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'round_players' AND column_name = 'same_bet_actions'
);
PREPARE phase_3_statement FROM @migration_sql;
EXECUTE phase_3_statement;
DEALLOCATE PREPARE phase_3_statement;

SET @migration_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE round_players ADD COLUMN final_bet_turns TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER same_bet_actions',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'round_players' AND column_name = 'final_bet_turns'
);
PREPARE phase_3_statement FROM @migration_sql;
EXECUTE phase_3_statement;
DEALLOCATE PREPARE phase_3_statement;

SET @migration_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE game_rounds ADD COLUMN final_two_started BOOLEAN NOT NULL DEFAULT FALSE AFTER forced_same_turns_remaining',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'game_rounds' AND column_name = 'final_two_started'
);
PREPARE phase_3_statement FROM @migration_sql;
EXECUTE phase_3_statement;
DEALLOCATE PREPARE phase_3_statement;
