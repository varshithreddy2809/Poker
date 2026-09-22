-- DEVELOPMENT/OPERATIONS ONLY. This script promotes an already registered
-- account to ADMIN. It never creates a user and never handles a raw password.
-- Register the account normally first, then replace the placeholder below and
-- run this manually against the intended database.
USE card_game_platform;

SET @admin_username = 'REPLACE_WITH_EXISTING_USERNAME';

UPDATE players
SET account_role = 'ADMIN'
WHERE username = @admin_username AND account_status = 'ACTIVE';

-- Exactly one row must be returned and its role must be ADMIN before login.
SELECT player_id, username, account_role
FROM players
WHERE username = @admin_username;
