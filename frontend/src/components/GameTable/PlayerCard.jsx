const SUITS = { S: "♠", H: "♥", C: "♣", D: "♦" };

export default function PlayerCard({ card, hidden }) {
  if (hidden || !card) return <div className="table-card table-card--back" aria-label="Face-down card"><span>♠</span></div>;
  const red = ["H", "D"].includes(card.suit);
  return <div className={`table-card ${red ? "table-card--red" : ""}`}><span>{card.rank === "T" ? "10" : card.rank}</span><strong>{SUITS[card.suit]}</strong></div>;
}
