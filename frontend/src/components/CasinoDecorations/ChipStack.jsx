import PokerChip from "./PokerChip.jsx";

export default function ChipStack({ color = "gold", count = 4, position, depth = "2", optional = false }) {
  return <div className={`chip-stack-decoration decoration--${position} ${optional ? "decoration--optional" : ""}`} data-depth={depth} aria-label={`Stack of ${count} decorative poker chips`}>
    {Array.from({ length: count }, (_, index) => <span className="chip-stack-decoration__chip" style={{ "--stack-index": index }} key={index}><PokerChip color={color} label={index === count - 1 ? "A" : "♛"} delay={`${-index * .5}s`} /></span>)}
  </div>;
}

