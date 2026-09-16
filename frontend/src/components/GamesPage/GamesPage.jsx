import GameCard from "./GameCard.jsx";
import Footer from "../Footer/Footer.jsx";
import "./GamesPage.css";

const GAMES = [
  { title: "Teen Patti", description: "Take a seat at the live virtual table for the classic three-card game.", icon: "teenPatti", available: true },
  { title: "Poker", description: "A premium Poker table experience is in preparation.", icon: "poker", available: false },
  { title: "Rummy", description: "Build your runs and sets when Rummy joins the collection.", icon: "rummy", available: false },
  { title: "Blackjack", description: "A new take on the classic 21-card table is on its way.", icon: "blackjack", available: false },
];

export default function GamesPage({ onPlayTeenPatti }) {
  return <div className="games-page"><section className="games-page__hero motion-fade-up" aria-labelledby="games-title"><p className="type-label">Choose your table</p><h1 id="games-title">The card room</h1><p>Classic games, a premium table, and more formats arriving over time.</p></section><section className="games-page__grid" aria-label="Available and upcoming games">{GAMES.map((game) => <GameCard key={game.title} {...game} onPlay={game.available ? onPlayTeenPatti : undefined} />)}<article className="games-page__more"><span aria-hidden="true">✦</span><h2>More Games Coming Soon</h2><p>The Aceverse collection will keep growing. Check back for the next seat at the table.</p></article></section><Footer /></div>;
}
