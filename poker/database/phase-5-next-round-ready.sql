-- Phase 5: a seated player must explicitly opt in to each replay round.
-- Run once after phases 1-4 on MySQL 8.0.
USE card_game_platform;

SET @schema_name = DATABASE();
SET @migration_sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE game_players ADD COLUMN next_round_ready BOOLEAN NOT NULL DEFAULT FALSE AFTER player_status',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'game_players' AND column_name = 'next_round_ready'
);
PREPARE phase_5_statement FROM @migration_sql;
EXECUTE phase_5_statement;
DEALLOCATE PREPARE phase_5_statement;
