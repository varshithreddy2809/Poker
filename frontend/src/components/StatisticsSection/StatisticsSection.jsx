import StatCard from "./StatCard.jsx";
import "./StatisticsSection.css";

const CAPABILITIES = [
  { value: "8", label: "Max Players", detail: "Seats available at a single game table." },
  { value: "1 Deck", label: "Per Table", detail: "A focused deck setup for every hand." },
  { value: "24/7", label: "Play Anytime", detail: "Start a virtual table whenever you are ready." },
  { value: "Multiple", label: "Card Games", detail: "A platform designed to grow beyond one game." },
];

export default function StatisticsSection() {
  return <section className="statistics-section" id="statistics" aria-labelledby="statistics-title"><div className="statistics-section__heading motion-fade-up"><p className="type-label">Platform capabilities</p><h2 id="statistics-title">Made for every hand</h2><p>Thoughtful table settings and game formats, built into the Aceverse experience.</p></div><div className="statistics-section__grid">{CAPABILITIES.map((capability) => <StatCard key={capability.label} {...capability} />)}</div></section>;
}
