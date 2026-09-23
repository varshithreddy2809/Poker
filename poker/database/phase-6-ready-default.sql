-- Phase 6: waiting-lobby readiness starts false for every newly seated player.
USE card_game_platform;

ALTER TABLE game_players
    ALTER COLUMN next_round_ready SET DEFAULT FALSE;
