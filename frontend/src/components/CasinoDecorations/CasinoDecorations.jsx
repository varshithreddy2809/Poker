import FloatingCard from "./FloatingCard.jsx";
import PokerChip from "./PokerChip.jsx";
import ChipStack from "./ChipStack.jsx";
import "./CasinoDecorations.css";

export default function CasinoDecorations() {
  return <aside className="casino-decorations" aria-label="Interactive decorative casino objects">
    <FloatingCard rank="A" suit="spades" position="card-left-main" rotation="-20deg" depth="3" delay="-1s" duration="8s" />
    <FloatingCard rank="K" suit="diamonds" position="card-left-top" rotation="18deg" depth="1" delay="-4s" duration="10s" optional />
    <FloatingCard rank="A" suit="hearts" position="card-right-main" rotation="16deg" depth="3" delay="-3s" duration="9s" />
    <FloatingCard rank="Q" suit="clubs" position="card-right-top" rotation="-16deg" depth="1" delay="-6s" duration="11s" optional />
    <ChipStack color="burgundy" count={5} position="chips-left" depth="2" />
    <ChipStack color="purple" count={4} position="chips-right" depth="2" />
    <PokerChip color="gold" label="A" delay="-5s" />
    <PokerChip color="navy" label="♛" delay="-8s" />
  </aside>;
}

