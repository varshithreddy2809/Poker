const ICONS = {
  multiplayer: <><circle cx="12" cy="8" r="3" /><path d="M5 21v-1a5 5 0 0 1 10 0v1M17 11a3 3 0 0 1 0-6M19 21v-1a5 5 0 0 0-3-4.58" /></>,
  voice: <><path d="M12 18a4 4 0 0 0 4-4V8a4 4 0 0 0-8 0v6a4 4 0 0 0 4 4Z" /><path d="M5 12v2a7 7 0 0 0 14 0v-2M12 21v-3" /></>,
  cards: <><rect x="6" y="4" width="12" height="16" rx="2" /><path d="m9 8 3-2 3 2-3 2-3-2ZM9 16l3 2 3-2" /></>,
  coins: <><circle cx="12" cy="12" r="8" /><path d="M14.5 9.5c-.4-.6-1.2-1-2.4-1-1.4 0-2.4.7-2.4 1.7 0 2.7 5 1.1 5 3.8 0 1.1-1 1.8-2.5 1.8-1.3 0-2.3-.4-2.8-1.1M12 7v10" /></>,
  eye: <><path d="M2.5 12s3.3-5 9.5-5 9.5 5 9.5 5-3.3 5-9.5 5-9.5-5-9.5-5Z" /><circle cx="12" cy="12" r="2.3" /></>,
  showdown: <><path d="M4 6h11v13H4zM9 3h11v13h-3" /><path d="m7 10 2 2 3-3M17 17l2 2m0-2-2 2" /></>,
};

export default function FeatureCard({ icon, title, description }) {
  return <article className="feature-card">
    <div className="feature-card__icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.65" strokeLinecap="round" strokeLinejoin="round">
        {ICONS[icon]}
      </svg>
    </div>
    <h3>{title}</h3>
    <p>{description}</p>
  </article>;
}
