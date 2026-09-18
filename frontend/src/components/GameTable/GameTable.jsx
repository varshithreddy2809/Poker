import GameHeader from "./GameHeader.jsx";
import GameControls from "./GameControls.jsx";
import PokerTable from "./PokerTable.jsx";
import BettingStage from "./BettingStage.jsx";
import CoinBorrowingPanel from "./CoinBorrowingPanel.jsx";
import SpectatorPanel from "./SpectatorPanel.jsx";
import "./GameTable.css";
import "./GameViewport.css";

export default function GameTable({ session, table, round, hand, showdown, live, run, leaveRound, playAgain, borrowing, borrow, refreshBorrowing }) {
  const mine = round.players?.find((player) => player.playerId === session.player.playerId);
  const isYourTurn = round.currentTurnPlayerId === session.player.playerId;
  const players = showdown?.hands ? showdown.hands.map((player) => ({ ...player, status: "ACTIVE", visibility: "SHOWDOWN", isTurn: false })) : (round.players || []).map((player) => ({ ...player, isTurn: player.playerId === round.currentTurnPlayerId }));
  const status = round.phase === "CHOOSING_VISIBILITY" ? "Choose Blind or Seen before the server deadline." : round.phase === "FINAL_TWO" ? "15× LIMIT REACHED — FINAL TWO PLAYERS." : round.phase === "FORCED_SAME" ? `FINAL SAME PHASE — TURN ${5 - Number(round.forcedSameTurnsRemaining || 0)} / 5.` : round.phase === "SIDE_SHOW_PENDING" ? "Side Show requested — waiting for a response." : isYourTurn ? "Your turn: choose an available action." : "Waiting for the current player.";
  return <section className="teen-game-table"><GameHeader table={table} round={round} isYourTurn={isYourTurn} live={live} onBack={leaveRound} /><BettingStage table={table} round={round} /><PokerTable players={players} session={session} hand={hand} showdown={showdown} round={round} />{!showdown && <p className="teen-game-table__status">{status}</p>}{!showdown && mine && <GameControls round={round} mine={mine} isYourTurn={isYourTurn} run={run} leaveRound={leaveRound} />}{!showdown && mine && <CoinBorrowingPanel round={round} session={session} borrowing={borrowing} borrow={borrow} refreshBorrowing={refreshBorrowing} />}{!showdown && !mine && <SpectatorPanel round={round} run={run} />}{showdown && <div className="teen-game-table__showdown"><strong>{showdown.winnerUsername} wins</strong><span>{showdown.winningHandCategory.replaceAll("_", " ")}</span>{table.hostPlayerId === session.player.playerId && <button className="primary" onClick={playAgain}>Play again</button>}</div>}</section>;
}
