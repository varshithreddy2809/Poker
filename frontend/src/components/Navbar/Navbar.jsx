import { useEffect, useState } from "react";
import "./Navbar.css";

const NAV_ITEMS = ["Home", "Games", "Features", "How to Play", "About", "Contact"];

export function BrandMark({ onHome }) {
  return <button className="ace-brand" type="button" onClick={onHome} aria-label="Aceverse home">
    <span className="ace-brand__crown" aria-hidden="true">♛</span>
    <span className="ace-brand__wordmark">ACEVERSE<small>Play Beyond Limits</small></span>
  </button>;
}

export function ThemeToggle() {
  const [royal, setRoyal] = useState(() => document.documentElement.dataset.theme === "royal");
  function toggleTheme() {
    const next = !royal;
    setRoyal(next);
    document.documentElement.dataset.theme = next ? "royal" : "midnight";
  }
  return <button className="navbar-icon-button" type="button" onClick={toggleTheme} aria-label={royal ? "Use midnight theme" : "Use royal theme"} title="Toggle color theme">
    <span aria-hidden="true">{royal ? "☾" : "☼"}</span>
  </button>;
}

function NavLinks({ className = "", onNavigate, activeItem }) {
  return <div className={`ace-nav-links ${className}`} aria-label="Primary navigation">
    {NAV_ITEMS.map((item) => <button className={item === activeItem ? "is-active" : ""} type="button" key={item} onClick={() => onNavigate(item)}>{item}</button>)}
  </div>;
}

function MobileMenu({ open, onClose, onNavigate, onLogin, onRegister, activeItem }) {
  useEffect(() => {
    if (!open) return undefined;
    const closeOnEscape = (event) => { if (event.key === "Escape") onClose(); };
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [open, onClose]);

  return <div className={`ace-mobile-layer ${open ? "is-open" : ""}`} aria-hidden={!open}>
    <button className="ace-mobile-backdrop" type="button" tabIndex={open ? 0 : -1} aria-label="Close navigation menu" onClick={onClose} />
    <div className="ace-mobile-menu" role="dialog" aria-label="Navigation menu">
      <NavLinks className="ace-mobile-links" activeItem={activeItem} onNavigate={(item) => { onNavigate(item); onClose(); }} />
      <div className="ace-mobile-actions">
        <button className="ace-login-button" type="button" onClick={() => { onLogin(); onClose(); }}>Login</button>
        <button className="ace-register-button" type="button" onClick={() => { onRegister(); onClose(); }}>♟ <span>Register</span></button>
      </div>
    </div>
  </div>;
}

export default function Navbar({ onLogin, onRegister, onNavigate = () => {}, activeItem = "Home" }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = (item) => onNavigate(item);

  return <header className="ace-navbar">
    <nav className="ace-navbar__inner" aria-label="Aceverse navigation">
      <BrandMark onHome={() => navigate("Home")} />
      <NavLinks className="ace-nav-links--desktop" activeItem={activeItem} onNavigate={navigate} />
      <div className="ace-navbar__actions">
        <ThemeToggle />
        <button className="ace-login-button" type="button" onClick={onLogin}>Login</button>
        <button className="ace-register-button" type="button" onClick={onRegister}>♟ <span>Register</span></button>
        <button className="ace-menu-button" type="button" aria-label="Open navigation menu" aria-expanded={mobileOpen} onClick={() => setMobileOpen(true)}><span /><span /><span /></button>
      </div>
    </nav>
    <MobileMenu open={mobileOpen} onClose={() => setMobileOpen(false)} onNavigate={navigate} onLogin={onLogin} onRegister={onRegister} activeItem={activeItem} />
  </header>;
}
