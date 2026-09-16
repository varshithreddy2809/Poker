import { useEffect, useState } from "react";

function secondsRemaining(deadline, now) { return deadline ? Math.max(0, Math.ceil((new Date(deadline).getTime() - now) / 1000)) : 0; }
export default function TurnTimer({ deadline, isYourTurn }) {
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => { const timer = window.setInterval(() => setNow(Date.now()), 250); return () => window.clearInterval(timer); }, []);
  const seconds = secondsRemaining(deadline, now);
  const display = `${String(Math.floor(seconds / 60)).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`;
  return <div className={`turn-timer ${isYourTurn ? "turn-timer--active" : ""}`} aria-live="polite"><span>{isYourTurn ? "Your turn" : "Turn timer"}</span><strong>{display}</strong></div>;
}
