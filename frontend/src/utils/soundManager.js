const preferenceKey = "aceverse:sound-enabled";

const effects = {
  deal: [[510, 0, 0.07, 0.018], [670, 0.06, 0.08, 0.014]],
  turn: [[660, 0, 0.12, 0.022]],
  warning: [[880, 0, 0.08, 0.025], [880, 0.12, 0.08, 0.02]],
  same: [[430, 0, 0.07, 0.018]],
  bet: [[520, 0, 0.08, 0.02]],
  raise: [[520, 0, 0.07, 0.02], [760, 0.06, 0.1, 0.022]],
  drop: [[240, 0, 0.13, 0.02]],
  sideShowRequest: [[620, 0, 0.08, 0.018], [740, 0.08, 0.08, 0.016]],
  sideShowAccept: [[720, 0, 0.1, 0.018]],
  sideShowResult: [[400, 0, 0.08, 0.017], [590, 0.08, 0.12, 0.018]],
  winner: [[660, 0, 0.12, 0.025], [830, 0.12, 0.16, 0.024]],
  error: [[180, 0, 0.1, 0.015]],
  newRound: [[460, 0, 0.08, 0.016], [610, 0.08, 0.1, 0.018]],
};

class SoundManager {
  constructor() {
    this.enabled = typeof window === "undefined" || window.localStorage.getItem(preferenceKey) !== "false";
    this.context = null;
    this.playedKeys = new Set();
  }

  isEnabled() { return this.enabled; }

  setEnabled(enabled) {
    this.enabled = enabled;
    try { window.localStorage.setItem(preferenceKey, String(enabled)); } catch { /* Storage is optional. */ }
  }

  async unlock() {
    try {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (!AudioContext) return false;
      if (!this.context || this.context.state === "closed") this.context = new AudioContext();
      if (this.context.state === "suspended") await this.context.resume();
      return this.context.state === "running";
    } catch { return false; }
  }

  play(effect, key) {
    if (!this.enabled || !effects[effect] || !this.context || this.context.state !== "running") return false;
    if (key && this.playedKeys.has(key)) return false;
    if (key) {
      this.playedKeys.add(key);
      if (this.playedKeys.size > 200) this.playedKeys.delete(this.playedKeys.values().next().value);
    }
    try {
      const now = this.context.currentTime;
      effects[effect].forEach(([frequency, delay, duration, volume]) => {
        const oscillator = this.context.createOscillator();
        const gain = this.context.createGain();
        oscillator.type = "sine";
        oscillator.frequency.setValueAtTime(frequency, now + delay);
        gain.gain.setValueAtTime(volume, now + delay);
        gain.gain.exponentialRampToValueAtTime(0.001, now + delay + duration);
        oscillator.connect(gain).connect(this.context.destination);
        oscillator.start(now + delay);
        oscillator.stop(now + delay + duration);
      });
      return true;
    } catch { return false; }
  }

  close() {
    const context = this.context;
    this.context = null;
    this.playedKeys.clear();
    void context?.close().catch(() => {});
  }
}

export const soundManager = new SoundManager();
