# Friendly Teen Patti API

This is a server-authoritative, no-betting game flow. There are no wallet,
coin transfer, entry fee, bet, raise, debt, or payment endpoints.

All requests except player registration require HTTP Basic authentication with
the registered `username` and password. This also ensures that a player cannot
request another player's private hand.

## 1. Register players

`POST /api/players`

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "friendly-game-password"
}
```

Register a second player the same way. Keep the returned `playerId` values.

## 2. Create a table as the host

`POST /api/tables`

```json
{
  "hostPlayerId": 1,
  "tableName": "Friday Friends",
  "maxPlayers": 4
}
```

Authenticate this request as `alice`. The response contains `tableId`.

## 3. Join a player to the table

`POST /api/tables/{tableId}/players`

```json
{
  "playerId": 2
}
```

Authenticate as the joining player.

## 4. Start a round

`POST /api/tables/{tableId}/rounds`

```json
{
  "hostPlayerId": 1
}
```

Only the host may start the round. The server creates and shuffles a fresh
52-card deck, deals three unique cards to each seated player, and returns a
`roundId`.

## 5. View only your own hand

`GET /api/rounds/{roundId}/players/{yourPlayerId}/hand`

The authenticated username must own `yourPlayerId`; another player's cards are
never returned before showdown.

## 6. Reveal and settle the friendly round

`POST /api/rounds/{roundId}/showdown`

```json
{
  "hostPlayerId": 1
}
```

The server evaluates Trail, Pure Sequence, Sequence, Color, Pair, and High
Card in that order, using suit order Spades > Hearts > Clubs > Diamonds as the
final tie-breaker. It reveals the completed hands, records the winner, and
reopens the table for another round.

## PowerShell example

```powershell
$basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('alice:friendly-game-password'))
$headers = @{ Authorization = "Basic $basic" }
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/tables/1' -Headers $headers
```
