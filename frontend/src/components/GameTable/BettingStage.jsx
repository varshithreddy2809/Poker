import "./BettingStage.css";

export default function BettingStage({ table, round }) {
  const threshold = Number(table.entryBet) * 15;
  const atThreshold = Number(round.currentBet) >= threshold;
  const stage = round.phase === "FORCED_SAME" ? "Final Same Phase" : round.phase === "FINAL_TWO" ? "Final Two Players" : round.phase === "SIDE_SHOW_PENDING" ? "Side Show Pending" : round.phase === "BETTING" ? "Betting" : "Visibility Choice";
  const completedTurns = 5 - Number(round.forcedSameTurnsRemaining || 0);
  return <aside className="betting-stage" aria-label="Server betting stage"><span>Starting bet <strong>{table.entryBet}</strong></span><span>15× limit <strong>{threshold}</strong></span><span>Current bet <strong>{round.currentBet}</strong></span><span>Stage <strong>{stage}</strong></span>{round.phase === "FORCED_SAME" && <span>Final SAME turn <strong>{completedTurns} / 5</strong></span>}{atThreshold && <small>{round.phase === "FINAL_TWO" ? "15× limit reached — FINAL TWO PLAYERS." : "15× limit reached."}</small>}</aside>;
}
