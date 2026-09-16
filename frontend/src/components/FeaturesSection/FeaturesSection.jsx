import FeatureCard from "./FeatureCard.jsx";
import "./FeaturesSection.css";

const FEATURES = [
  { icon: "multiplayer", title: "Multiplayer", description: "Play with other players in real time." },
  { icon: "voice", title: "Voice Chat", description: "Communicate with players using live voice chat." },
  { icon: "cards", title: "Multiple Games", description: "Teen Patti, Poker, Rummy, and future card games." },
  { icon: "coins", title: "Virtual Coins", description: "Play using virtual coins and betting rules." },
  { icon: "eye", title: "Blind & Seen", description: "Choose Blind or Seen after cards are dealt." },
  { icon: "showdown", title: "Side Show", description: "Request and resolve Side Shows between players." },
];

export default function FeaturesSection() {
  return <section className="features-section" id="features" aria-labelledby="features-title">
    <div className="features-section__heading motion-fade-up">
      <p className="type-label">Built for the table</p>
      <h2 id="features-title">More ways to play</h2>
      <p>Every hand brings players together with classic card-table features.</p>
    </div>
    <div className="features-section__grid">
      {FEATURES.map((feature) => <FeatureCard key={feature.title} {...feature} />)}
    </div>
  </section>;
}
