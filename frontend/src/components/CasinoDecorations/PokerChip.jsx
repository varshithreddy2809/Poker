import { useState } from "react";

export default function PokerChip({ color = "gold", label = "A", delay = "0s" }) {
  const [selected, setSelected] = useState(false);
  return <button type="button" className={`poker-chip poker-chip--${color} ${selected ? "is-selected" : ""}`}
    aria-label={`Decorative ${color} poker chip`} aria-pressed={selected}
    style={{ "--chip-delay": delay }} onClick={() => setSelected((current) => !current)}>
    <span className="poker-chip__rim">
      <span className="poker-chip__ring"><b>{label}</b><i>ACEVERSE</i></span>
    </span>
    <span className="poker-chip__shine" />
  </button>;
}

