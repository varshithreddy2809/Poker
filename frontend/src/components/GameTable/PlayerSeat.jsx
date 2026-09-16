import PlayerHand from "./PlayerHand.jsx";

export default function PlayerSeat({ player, index, total, own, cards, showdown }) {
  const isTurn = player.playerId && player.isTurn; const dropped = player.status === "DROPPED";
  return <article className={`player-seat ${own ? "player-seat--self" : ""} ${isTurn ? "player-seat--turn" : ""} ${dropped ? "player-seat--dropped" : ""}`} style={{ "--seat-index": index, "--seat-total": total }}><div className="player-seat__avatar" aria-hidden="true">{player.username?.slice(0, 1).toUpperCase()}</div><div className="player-seat__name"><strong>{player.username}{own ? " (You)" : ""}</strong><span>{dropped ? "Dropped" : player.visibility === "PENDING" ? "Choosing" : player.visibility}</span></div><div className="player-seat__meta"><span>{player.totalContribution ?? "—"} bet</span><span className="player-seat__connection">● Connected</span></div><PlayerHand cards={cards} hidden={!showdown && !own} />{isTurn && <span className="player-seat__turn">Current turn</span>}</article>;
}
