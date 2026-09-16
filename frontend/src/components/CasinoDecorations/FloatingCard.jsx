import { useState } from "react";

const SUITS = { spades: "\u2660", hearts: "\u2665", diamonds: "\u2666", clubs: "\u2663" };

export default function FloatingCard({ rank = "A", suit = "spades", position, rotation = "-12deg", depth = "2", delay = "0s", duration = "7s", optional = false }) {
  const [inspected, setInspected] = useState(false);
  const symbol = SUITS[suit] || SUITS.spades;
  return <button type="button" className={`floating-card decoration--${position} ${optional ? "decoration--optional" : ""} ${inspected ? "is-inspected" : ""}`}
    aria-label={`Decorative ${rank} of ${suit}`} aria-pressed={inspected} data-depth={depth}
    style={{ "--card-rotation": rotation, "--card-delay": delay, "--card-duration": duration }}
    onClick={() => setInspected((current) => !current)}>
    <span className={`floating-card__body suit--${suit}`}>
      <span className="floating-card__corner"><b>{rank}</b><i>{symbol}</i></span>
      <span className="floating-card__center">{symbol}</span>
      <span className="floating-card__corner floating-card__corner--bottom"><b>{rank}</b><i>{symbol}</i></span>
      <span className="floating-card__reflection" />
    </span>
  </button>;
}

