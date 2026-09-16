import "./CinematicBackground.css";

const BOKEH = [
  ["8%", "20%", "10px", ".30", "0s", "11s"],
  ["17%", "66%", "6px", ".36", "-4s", "13s"],
  ["29%", "14%", "8px", ".20", "-7s", "15s"],
  ["72%", "18%", "12px", ".22", "-3s", "14s"],
  ["84%", "36%", "7px", ".35", "-9s", "12s"],
  ["91%", "73%", "11px", ".24", "-5s", "16s"],
  ["61%", "81%", "6px", ".30", "-11s", "10s"],
  ["43%", "72%", "5px", ".25", "-2s", "13s"],
];

const PARTICLES = [
  ["13%", "39%", "1px", "-2s"], ["23%", "76%", "2px", "-6s"],
  ["35%", "29%", "1px", "-9s"], ["54%", "12%", "2px", "-1s"],
  ["68%", "58%", "1px", "-7s"], ["78%", "84%", "2px", "-4s"],
  ["87%", "16%", "1px", "-10s"], ["94%", "52%", "2px", "-3s"],
];

const styleFor = ([left, top, size, opacity, delay, duration]) => ({
  "--particle-left": left, "--particle-top": top, "--particle-size": size,
  "--particle-opacity": opacity, "--particle-delay": delay, "--particle-duration": duration,
});

export default function CinematicBackground() {
  return <div className="cinematic-background" aria-hidden="true">
    <div className="cinematic-base" data-depth="0" />
    <div className="cinematic-ambient" data-depth="1">
      <i className="ambient-light ambient-light--gold" />
      <i className="ambient-light ambient-light--violet" />
      <i className="ambient-light ambient-light--navy" />
    </div>
    <div className="cinematic-beams" data-depth="1">
      <i className="light-beam light-beam--one" />
      <i className="light-beam light-beam--two" />
      <i className="light-beam light-beam--three" />
    </div>
    <div className="cinematic-casino" data-depth="2">
      <i className="casino-halo" />
      <i className="casino-table-rim" />
      <i className="casino-reflection" />
    </div>
    <div className="cinematic-bokeh" data-depth="2">
      {BOKEH.map((item, index) => <i className="bokeh-particle" style={styleFor(item)} key={index} />)}
    </div>
    <div className="cinematic-particles" data-depth="3">
      {PARTICLES.map(([left, top, size, delay], index) => <i className="floating-particle" style={{ "--particle-left": left, "--particle-top": top, "--particle-size": size, "--particle-delay": delay }} key={index} />)}
    </div>
    <div className="cinematic-vignette" data-depth="0" />
  </div>;
}

