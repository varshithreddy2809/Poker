-- DESTRUCTIVE: local development data only.
--
-- Removes every registered player and the game, balance, borrowing, and
-- authentication-related records that depend on a player. It preserves the
-- database, every table, foreign key, index, and the game_types catalog.
--
-- Run manually, never from application startup:
--   mysql -u root -p card_game_platform < database/reset-development-users.sql

USE card_game_platform;

-- Capture the removal totals before deleting anything.
SELECT 'before' AS reset_stage,
       (SELECT COUNT(*) FROM players) AS players,
       (SELECT COUNT(*) FROM game_tables) AS game_tables,
       (SELECT COUNT(*) FROM game_players) AS game_players,
       (SELECT COUNT(*) FROM game_rounds) AS game_rounds,
       (SELECT COUNT(*) FROM round_players) AS round_players,
       (SELECT COUNT(*) FROM player_cards) AS player_cards,
       (SELECT COUNT(*) FROM bets) AS bets,
       (SELECT COUNT(*) FROM game_actions) AS game_actions,
       (SELECT COUNT(*) FROM side_shows) AS side_shows,
       (SELECT COUNT(*) FROM wallet_transactions) AS wallet_transactions,
       (SELECT COUNT(*) FROM player_debts) AS player_debts,
       (SELECT COUNT(*) FROM debt_transactions) AS debt_transactions,
       (SELECT COUNT(*) FROM coin_borrow_requests) AS coin_borrow_requests,
       (SELECT COUNT(*) FROM admin_coin_transactions) AS admin_coin_transactions;

START TRANSACTION;

-- Delete children before their game-round, game-player, and player parents.
DELETE FROM debt_transactions;
DELETE FROM admin_coin_transactions;
DELETE FROM wallet_transactions;
DELETE FROM coin_borrow_requests;
DELETE FROM side_shows;
DELETE FROM game_actions;
DELETE FROM bets;
DELETE FROM round_players;
DELETE FROM player_cards;
DELETE FROM player_debts;
DELETE FROM game_rounds;
DELETE FROM game_players;
DELETE FROM game_tables;
DELETE FROM players;

COMMIT;

-- Verification: all player-owned records must be empty. game_types remains.
SELECT 'after' AS reset_stage,
       (SELECT COUNT(*) FROM players) AS players,
       (SELECT COUNT(*) FROM game_tables) AS game_tables,
       (SELECT COUNT(*) FROM game_players) AS game_players,
       (SELECT COUNT(*) FROM game_rounds) AS game_rounds,
       (SELECT COUNT(*) FROM round_players) AS round_players,
       (SELECT COUNT(*) FROM player_cards) AS player_cards,
       (SELECT COUNT(*) FROM bets) AS bets,
       (SELECT COUNT(*) FROM game_actions) AS game_actions,
       (SELECT COUNT(*) FROM side_shows) AS side_shows,
       (SELECT COUNT(*) FROM wallet_transactions) AS wallet_transactions,
       (SELECT COUNT(*) FROM player_debts) AS player_debts,
       (SELECT COUNT(*) FROM debt_transactions) AS debt_transactions,
       (SELECT COUNT(*) FROM coin_borrow_requests) AS coin_borrow_requests,
       (SELECT COUNT(*) FROM admin_coin_transactions) AS admin_coin_transactions,
       (SELECT COUNT(*) FROM game_types) AS preserved_game_types;
