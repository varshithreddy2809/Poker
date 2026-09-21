import assert from "node:assert/strict";
import test from "node:test";

let starts = 0;

class FakeAudioContext {
  constructor() { this.currentTime = 0; this.state = "suspended"; }
  async resume() { this.state = "running"; }
  async close() { this.state = "closed"; }
  createOscillator() {
    return {
      connect() { return this; }, frequency: { setValueAtTime() {} }, start() { starts += 1; }, stop() {}, type: "sine",
    };
  }
  createGain() {
    return { connect() { return this; }, gain: { exponentialRampToValueAtTime() {}, setValueAtTime() {} } };
  }
}

const preferences = new Map();
globalThis.window = {
  AudioContext: FakeAudioContext,
  localStorage: { getItem: (key) => preferences.get(key) ?? null, setItem: (key, value) => preferences.set(key, value) },
};

const { soundManager } = await import("./soundManager.js");

test("sound manager initializes safely, honors mute, and suppresses duplicate event keys", async () => {
  soundManager.setEnabled(false);
  assert.equal(soundManager.play("deal", "deal:1"), false);

  soundManager.setEnabled(true);
  assert.equal(await soundManager.unlock(), true);
  assert.equal(soundManager.play("deal", "deal:1"), true);
  assert.equal(soundManager.play("deal", "deal:1"), false);
  assert.equal(starts, 2, "the deal effect has two tones and must only play once");
  soundManager.close();
});
