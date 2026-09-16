import "./HeroPanel.css";
import "../Branding/Branding.css";
import AceverseWordmark from "../Branding/AceverseWordmark.jsx";
import Crown from "../Branding/Crown.jsx";
import GoldDivider from "../Branding/GoldDivider.jsx";

export default function HeroPanel({ onLogin, onRegister }) {
  return <section className="hero-panel motion-fade-up" id="home" data-depth="3" aria-labelledby="aceverse-hero-title">
    <div className="hero-panel__glow hero-panel__glow--gold" aria-hidden="true" />
    <div className="hero-panel__glow hero-panel__glow--violet" aria-hidden="true" />
    <div className="hero-panel__border-sweep" aria-hidden="true" />
    <div className="hero-panel__content">
      <Crown />
      <p className="hero-panel__welcome">Welcome to</p>
      <AceverseWordmark id="aceverse-hero-title" />
      <p className="hero-panel__tagline">More Games <span>•</span> More People <span>•</span> A Bigger Table</p>
      <GoldDivider />
      <p className="hero-panel__description">A premium online card game platform for friends, tables, and unforgettable hands.</p>
      <div className="hero-panel__actions" aria-label="Account actions">
        <button className="hero-panel__login-button" type="button" onClick={onLogin}>
          <span className="hero-panel__button-kicker">Already a player?</span>
          Login
        </button>
        <button className="hero-panel__register-button shine-on-hover" type="button" onClick={onRegister}>
          <span className="hero-panel__button-kicker">Join the table</span>
          Create Account
        </button>
      </div>
    </div>
  </section>;
}
