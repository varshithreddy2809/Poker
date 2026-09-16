import PlayerCard from "./PlayerCard.jsx";

export default function PlayerHand({ cards, hidden }) {
  const displayCards = cards?.length ? cards : [null, null, null];
  return <div className="player-hand-ui">{displayCards.map((card, index) => <PlayerCard key={card?.position || index} card={card} hidden={hidden} />)}</div>;
}
