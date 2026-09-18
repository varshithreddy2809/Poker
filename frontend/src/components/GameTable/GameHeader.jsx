import TurnTimer from "./TurnTimer.jsx";

export default function GameHeader({ table, round, isYourTurn, live, onBack }) {
  return <header className="teen-game-header"><div><button className="text-button" type="button" onClick={onBack}>← Back / Leave Game</button><p className="type-label">Teen Patti · Round {round.roundNumber}</p><h1>{table.tableName}</h1></div><div className="teen-game-header__hud"><span className={live ? "game-live game-live--on" : "game-live"}>{live ? "LIVE" : "RECONNECTING"}</span><TurnTimer deadline={round.turnDeadline || round.visibilityDeadline} isYourTurn={isYourTurn} /></div></header>;
}
