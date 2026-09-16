const ICONS = {
  teenPatti: <><path d="M7 4h10a2 2 0 0 1 2 2v14H7a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z" /><path d="M12 8v8m-3-5 3-3 3 3m-6 2 3 3 3-3" /></>,
  poker: <><path d="M6 4h12v16H6z" /><path d="m12 7 3 3-3 3-3-3 3-3Zm-3 9h6" /></>,
  rummy: <><path d="M4 7h11v13H4zM9 4h11v13h-3" /><path d="M7 11h5m-5 3h5" /></>,
  blackjack: <><rect x="5" y="4" width="14" height="16" rx="2" /><path d="M8 8h3m-3 3h3m5 4c0-2-1.5-3-3-3s-3 1-3 3" /></>,
};

export default function GameCard({ title, description, icon, available, onPlay }) {
  return <article className={`game-card ${available ? "game-card--available" : "game-card--coming"}`}>
    <div className="game-card__icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.65" strokeLinecap="round" strokeLinejoin="round">{ICONS[icon]}</svg></div>
    <span className="game-card__status">{available ? "Available now" : "Coming soon"}</span>
    <h2>{title}</h2>
    <p>{description}</p>
    <button type="button" className={available ? "game-card__play" : "game-card__soon"} onClick={available ? onPlay : undefined} disabled={!available}>{available ? "Play Teen Patti" : "Coming Soon"}</button>
  </article>;
}
