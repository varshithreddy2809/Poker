import GamePot from "./GamePot.jsx";
import PlayerSeat from "./PlayerSeat.jsx";

export default function PokerTable({ players, session, hand, showdown, round }) {
  const mine = players.find((player) => player.playerId === session.player.playerId);
  return <div className="poker-table"><div className="poker-table__rail" /><div className="poker-table__felt"><div className="poker-table__dealer" aria-label="Dealer deck"><span>♛</span><small>Dealer</small></div><GamePot pot={round.pot} currentBet={round.currentBet} contribution={mine?.totalContribution} balance={session.player.coinBalance} />{players.map((player, index) => <PlayerSeat key={player.playerId} player={player} index={index} total={players.length} own={player.playerId === session.player.playerId} cards={showdown ? player.cards : player.playerId === session.player.playerId ? hand?.cards : null} showdown={Boolean(showdown)} />)}</div></div>;
}
