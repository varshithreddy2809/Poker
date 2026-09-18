import { useState } from "react";

export default function SpectatorPanel({ round, run }) {
  const [selected, setSelected] = useState("");
  const players = (round.players || []).filter((player) => player.status === "ACTIVE");
  return <section className="coin-borrowing"><p className="type-label">Spectator Mode</p><h2>Watching public game state</h2><p className="coin-borrowing__balance">Private cards and game actions are unavailable to spectators.</p><label className="game-controls__raise">Follow player<select value={selected} onChange={(event) => setSelected(event.target.value)}><option value="">Select an active player</option>{players.map((player) => <option key={player.playerId} value={player.playerId}>{player.username}</option>)}</select></label><button className="secondary" type="button" disabled={!selected} onClick={() => run(`/rounds/${round.roundId}/spectate`, { selectedPlayerId: Number(selected) })}>Update selection</button></section>;
}
