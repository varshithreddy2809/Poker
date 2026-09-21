import { Client } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import { soundManager } from "./utils/soundManager";

const API = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");
const SUITS = { S: "♠", H: "♥", C: "♣", D: "♦" };

function auth(credentials) { return `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`; }
function wsUrl() { if (import.meta.env.VITE_WS_URL) return import.meta.env.VITE_WS_URL; return `${location.protocol === "https:" ? "wss" : "ws"}://${location.hostname}:8080/ws`; }
async function api(path, credentials, method = "GET", body) {
  const headers = { Accept: "application/json" }; if (credentials) headers.Authorization = auth(credentials); if (body !== undefined) headers["Content-Type"] = "application/json";
  const response = await fetch(`${API}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const payload = await response.json().catch(() => ({})); if (!response.ok) throw new Error(payload.message || `Request failed (${response.status})`); return payload;
}
function Card({ card, hidden }) {
  const suit = SUITS[card.suit] || card.suit;
  const rank = card.rank === "T" ? "10" : card.rank;
  return <div className={`card ${hidden ? "card-back" : ["H", "D"].includes(card.suit) ? "red" : ""}`} aria-label={hidden ? "Face-down card" : `${rank} of ${suit}`}>{hidden ? "?" : <><span>{rank}</span><strong aria-hidden="true">{suit}</strong></>}</div>;
}
function Message({ error, notice }) { return (error || notice) && <div className={`message ${error ? "error" : "notice"}`}>{error || notice}</div>; }

function useGameSounds(round, showdown, playerId) {
  const [soundEnabled, setSoundEnabled] = useState(() => soundManager.isEnabled());
  const dealtRoundRef = useRef(null); const turnRef = useRef(null); const warningRef = useRef(null); const previousRoundRef = useRef(null); const winnerRef = useRef(null);
  const playSound = useCallback((effect, key) => soundManager.play(effect, key), []);
  const unlockSound = useCallback(() => { void soundManager.unlock(); }, []);
  const toggleSound = useCallback(() => {
    setSoundEnabled((enabled) => {
      const next = !enabled;
      soundManager.setEnabled(next);
      if (next) void soundManager.unlock();
      return next;
    });
  }, []);

  useEffect(() => { soundManager.setEnabled(soundEnabled); }, [soundEnabled]);
  useEffect(() => {
    const dealKey = round?.phase === "CHOOSING_VISIBILITY" ? `deal:${round.roundId}` : null;
    if (!dealKey || dealtRoundRef.current === dealKey) return;
    const previousDeal = dealtRoundRef.current;
    dealtRoundRef.current = dealKey;
    playSound("deal", dealKey);
    if (previousDeal) playSound("newRound", `new-round:${round.roundId}`);
  }, [playSound, round?.phase, round?.roundId]);
  useEffect(() => {
    const turnKey = round && round.currentTurnPlayerId === playerId ? `${round.roundId}:${round.turnDeadline}` : null;
    if (!turnKey) { turnRef.current = null; warningRef.current = null; return undefined; }
    if (turnRef.current !== turnKey) { turnRef.current = turnKey; playSound("turn", `turn:${turnKey}`); }
    const warn = () => {
      const seconds = Math.ceil((new Date(round.turnDeadline).getTime() - Date.now()) / 1000);
      if (seconds > 0 && seconds <= 5 && warningRef.current !== turnKey) {
        warningRef.current = turnKey;
        playSound("warning", `warning:${turnKey}`);
      }
    };
    warn(); const timer = window.setInterval(warn, 500);
    return () => window.clearInterval(timer);
  }, [playSound, playerId, round]);
  useEffect(() => {
    const previous = previousRoundRef.current;
    previousRoundRef.current = round;
    if (!previous || !round || previous.roundId !== round.roundId) return;
    const dropped = (round.players || []).some((player) => previous.players?.find((old) => old.playerId === player.playerId)?.status === "ACTIVE" && player.status === "DROPPED");
    const sideShowResolved = previous.phase === "SIDE_SHOW_PENDING" && round.phase !== "SIDE_SHOW_PENDING" && dropped;
    if (sideShowResolved) {
      playSound("sideShowAccept", `side-show-accept:${round.roundId}:${previous.pendingSideShowId}`);
      playSound("sideShowResult", `side-show-result:${round.roundId}:${previous.pendingSideShowId}`);
    } else if (dropped) {
      playSound("drop", `drop:${round.roundId}:${round.pot}:${round.currentTurnPlayerId}`);
    }
    if (round.phase === "SIDE_SHOW_PENDING" && previous.phase !== "SIDE_SHOW_PENDING" && round.pendingSideShowTargetPlayerId === playerId) {
      playSound("sideShowRequest", `side-show-request:${round.roundId}:${round.pendingSideShowId}`);
    }
  }, [playSound, playerId, round]);
  useEffect(() => {
    const winnerKey = showdown ? `winner:${showdown.roundId}:${showdown.winnerPlayerId}` : null;
    if (!winnerKey || winnerRef.current === winnerKey) return;
    winnerRef.current = winnerKey;
    playSound("winner", winnerKey);
  }, [playSound, showdown]);
  useEffect(() => () => soundManager.close(), []);
  return { soundEnabled, toggleSound, unlockSound, playSound };
}

function Auth({ setSession, tell }) {
  const [register, setRegister] = useState(false); const [form, setForm] = useState({ username: "", email: "", password: "" }); const [busy, setBusy] = useState(false);
  const update = (name) => (event) => setForm({ ...form, [name]: event.target.value });
  async function submit(event) {
    event.preventDefault(); setBusy(true);
    try {
      const credentials = { username: form.username.trim(), password: form.password }; let player;
      if (register) player = await api("/players", null, "POST", { ...form, username: credentials.username });
      else { const id = sessionStorage.getItem(`aceverse:${credentials.username}`); if (!id) throw new Error("Register this player in this browser before signing in."); player = await api(`/players/${id}`, credentials); }
      sessionStorage.setItem(`aceverse:${player.username}`, player.playerId); setSession({ credentials, player }); tell("");
    } catch (error) { tell(error.message, true); } finally { setBusy(false); }
  }
  return <section className="panel auth-panel"><p className="eyebrow">Teen Patti - virtual coins only</p><h1>ACEVERSE</h1><div className="tabs"><button className={!register ? "selected" : ""} onClick={() => setRegister(false)}>Sign in</button><button className={register ? "selected" : ""} onClick={() => setRegister(true)}>Register</button></div><form className="form-grid" onSubmit={submit}><label>Username<input minLength="3" value={form.username} onChange={update("username")} required /></label>{register && <label>Email<input type="email" value={form.email} onChange={update("email")} required /></label>}<label>Password<input type="password" minLength="8" value={form.password} onChange={update("password")} required /></label><button className="primary wide" disabled={busy}>{register ? "Create player" : "Enter lobby"}</button></form></section>;
}

function Lobby({ session, table, setTable, start, signOut, tell }) {
  const [name, setName] = useState(`${session.player.username}'s table`); const [bet, setBet] = useState(100); const [max, setMax] = useState(4); const [tableId, setTableId] = useState(""); const [busy, setBusy] = useState(false);
  async function create(event) { event.preventDefault(); setBusy(true); try { setTable(await api("/tables", session.credentials, "POST", { hostPlayerId: session.player.playerId, tableName: name, entryBet: Number(bet), maxPlayers: Number(max) })); } catch (error) { tell(error.message, true); } finally { setBusy(false); } }
  async function join(event) { event.preventDefault(); setBusy(true); try { setTable(await api(`/tables/${tableId}/players`, session.credentials, "POST", { playerId: session.player.playerId })); } catch (error) { tell(error.message, true); } finally { setBusy(false); } }
  function confirmSignOut() { if (window.confirm("Sign out of ACEVERSE?")) if (window.confirm("Final confirmation: end this session now?")) signOut(); }
  if (!table) return <section className="lobby-page"><header className="lobby-intro"><div className="lobby-back-action"><button className="text-button" type="button" onClick={confirmSignOut}>← Back / Sign out</button></div></header><div className="lobby-grid"><form className="panel form-grid" onSubmit={create}><p className="eyebrow">Host a table</p><h2>Create game</h2><label>Table name<input value={name} onChange={(e) => setName(e.target.value)} required /></label><label>Starting bet<input type="number" min="1" value={bet} onChange={(e) => setBet(e.target.value)} required /></label><label>Maximum players<select value={max} onChange={(e) => setMax(e.target.value)}>{[2, 3, 4, 5, 6, 7, 8].map((n) => <option key={n}>{n}</option>)}</select></label><button className="primary" disabled={busy}>Create</button></form><form className="panel form-grid" onSubmit={join}><p className="eyebrow">Join game</p><h2>Enter table ID</h2><label>Table ID<input type="number" min="1" value={tableId} onChange={(e) => setTableId(e.target.value)} required /></label><button className="secondary" disabled={busy}>Join table</button></form></div></section>;
  const host = table.hostPlayerId === session.player.playerId;
  return <section className="panel table-lobby"><div className="table-heading"><div><button className="text-button" type="button" onClick={() => setTable(null)}>← Back to lobby</button><p className="eyebrow">Table #{table.tableId} - starting bet {table.entryBet}</p><h2>{table.tableName}</h2></div><span className={`status ${table.status.toLowerCase()}`}>{table.status}</span></div><p className="muted">Share this table ID. Each new player receives 10,000 virtual coins.</p><div className="seat-list">{table.players.map((p) => <div className="seat" key={p.playerId}><span className="seat-number">{p.seatNumber}</span>{p.username}{p.playerId === table.hostPlayerId && <em>Host</em>}</div>)}</div><div className="actions">{host && <button className="primary" disabled={table.players.length < 2 || busy} onClick={start}>{table.players.length < 2 ? "Waiting for player" : "Deal cards"}</button>}<button className="secondary" onClick={async () => { try { setTable(await api(`/tables/${table.tableId}`, session.credentials)); } catch (e) { tell(e.message, true); } }}>Refresh</button><button className="text-button" type="button" onClick={confirmSignOut}>Sign out</button></div></section>;
}

function BettingControls({ round, canSame, canShow, confirmLeave, run }) {
  const [betAdjust, setBetAdjust] = useState(round.currentBet + 1);
  const amount = Number(betAdjust);

  return <div className="betting-controls">
    <div className="betting-controls-top">
      <label className="bet-adjust-field">Bet Adjust<input id="bet-adjust" type="number" min={round.currentBet + 1} value={betAdjust} onChange={(e) => setBetAdjust(e.target.value)} /></label>
      {canShow && <button className="secondary" onClick={() => run(`/rounds/${round.roundId}/actions`, { action: "SHOW", amount })}>Show</button>}
      <button className="text-button" type="button" onClick={confirmLeave}>Drop</button>
    </div>
    <div className="betting-controls-bottom">
      <button className="primary" disabled={!canSame} onClick={() => run(`/rounds/${round.roundId}/actions`, { action: "SAME" })}>Same</button>
      <button className="secondary" onClick={() => run(`/rounds/${round.roundId}/actions`, { action: "RAISE", amount })}>Raise</button>
      <button className="secondary" disabled>Side Show</button>
    </div>
  </div>;
}

function Table({ session, table, round, hand, showdown, live, run, leaveRound, playAgain }) {
  const mine = round.players?.find((p) => p.playerId === session.player.playerId); const myTurn = round.currentTurnPlayerId === session.player.playerId; const canSame = round.phase === "FINAL_TWO" || (mine?.sameBetActions ?? 0) < 5; const canShow = (round.players || []).filter((player) => player.status === "ACTIVE" && player.live !== false).length === 2;
  const players = showdown?.hands || round.players || [];
  async function confirmLeave() { if (!window.confirm("Leave this game? You will be dropped from the current round.")) return; if (!window.confirm("Final confirmation: drop from this round now?")) return; await leaveRound(); }
  return <section className="game-board"><header className="game-header"><div><button className="text-button" type="button" onClick={confirmLeave}>← Back / Leave game</button><p className="eyebrow">Round {round.roundNumber} - pot {round.pot} - bet {round.currentBet}</p><h2>{table.tableName}</h2></div><div className="live-status"><span className={live ? "live-dot connected" : "live-dot"} />{live ? "LIVE" : "RECONNECTING"}</div></header>
    {!showdown && <div className="round-message">{round.phase === "CHOOSING_VISIBILITY" ? "Choose Blind or Seen. After 30 seconds unselected players become Blind." : round.phase === "FINAL_TWO" ? `Final betting: each remaining player has 5 turns (threshold ${round.finalTwoThreshold}).` : myTurn ? "Your turn. The server will auto-drop you after 30 seconds." : "Waiting for the current player's action."}</div>}
    <div className="players-grid">{players.map((p) => { const own = p.playerId === session.player.playerId; const cards = showdown ? p.cards || [] : own ? hand?.cards || [] : [1, 2, 3]; const counter = round.phase === "FINAL_TWO" ? `Final betting: ${p.finalBetTurns ?? 0}/${round.finalBetTurnLimit ?? 5}` : `SAME: ${p.sameBetActions ?? 0}/5`; return <article className={`player-hand ${showdown?.winnerPlayerId === p.playerId ? "winner" : ""}`} key={p.playerId}><div className="player-name">{p.username}{own && " (you)"}</div><div className="cards">{cards.map((c, i) => <Card key={c.position || i} card={c} hidden={!showdown && !own} />)}</div><div className="category">{showdown ? p.handCategory?.replaceAll("_", " ") : <><div>{p.visibility}</div><div>{p.status}</div>{p.status === "ACTIVE" && <div>{counter}</div>}</>}</div></article>; })}</div>
    {!showdown && mine?.visibility === "PENDING" && <div className="actions centered"><button className="primary" onClick={() => run(`/rounds/${round.roundId}/visibility`, { choice: "BLIND" })}>Play Blind</button><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/visibility`, { choice: "SEEN" })}>See Cards</button></div>}
    {!showdown && mine?.visibility === "BLIND" && <div className="actions centered"><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/see-cards`)}>See my cards</button></div>}
    {!showdown && myTurn && ["BETTING", "FINAL_TWO"].includes(round.phase) && <BettingControls key={`${round.roundId}:${round.currentBet}`} round={round} canSame={canSame} canShow={canShow} confirmLeave={confirmLeave} run={run} />}
    {!showdown && round.phase === "SIDE_SHOW_PENDING" && round.pendingSideShowTargetPlayerId === session.player.playerId && <div className="actions centered"><button className="primary" onClick={() => run(`/rounds/${round.roundId}/side-shows/${round.pendingSideShowId}/response`, { response: "ACCEPT" })}>Accept Side Show</button><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/side-shows/${round.pendingSideShowId}/response`, { response: "REJECT" })}>Reject</button></div>}
    {!showdown && mine?.status === "ACTIVE" && !myTurn && <div className="actions centered"><button className="text-button" type="button" onClick={confirmLeave}>Leave game / Drop</button></div>}
    {showdown && <div className="round-message">{showdown.winnerUsername} wins: {showdown.winningHandCategory.replaceAll("_", " ")}</div>}{showdown && <div className="actions centered"><button className="primary" onClick={playAgain}>Play again</button></div>}
  </section>;
}

export default function Game() {
  const [session, setSession] = useState(null); const [table, setTable] = useState(null); const [round, setRound] = useState(null); const [hand, setHand] = useState(null); const [showdown, setShowdown] = useState(null); const [live, setLive] = useState(false); const [error, setError] = useState(""); const [notice, setNotice] = useState("");
  const sessionCredentials = session?.credentials; const sessionPlayerId = session?.player?.playerId;
  const { soundEnabled, toggleSound, unlockSound, playSound } = useGameSounds(round, showdown, sessionPlayerId);
  const tell = useCallback((text, isError = false) => { setError(isError ? text : ""); setNotice(isError ? "" : text); }, []);
  useEffect(() => {
    if (!sessionCredentials || !sessionPlayerId || !table?.tableId) return undefined;

    const tableId = table.tableId;
    const credentials = sessionCredentials;
    let active = true;
    let subscription;
    let syncVersion = 0;

    const syncTableState = async (event) => {
      const requestVersion = ++syncVersion;
      if (event?.type === "SHOWDOWN") {
        if (event.round) setRound(event.round);
        setShowdown(event.showdown);
        setHand(null);
      } else if (event?.round) {
        setRound(event.round);
        setShowdown(null);
        setHand(null);
      }
      try {
        const [latestTable, latestPlayer] = await Promise.all([
          api(`/tables/${tableId}`, credentials),
          api(`/players/${sessionPlayerId}`, credentials),
        ]);
        if (!active || requestVersion !== syncVersion) return;

        setTable(latestTable);
        setSession((current) => current ? { ...current, player: latestPlayer } : current);
        if (event?.type === "SHOWDOWN" || event?.round) return;
        if (latestTable.status === "IN_GAME") {
          const activeRound = await api(`/tables/${tableId}/rounds/active`, credentials);
          if (!active || requestVersion !== syncVersion) return;
          setRound(activeRound);
          setShowdown(null);
          setHand(null);
        } else {
          setRound(null);
          setHand(null);
        }
      } catch (error) {
        if (active && requestVersion === syncVersion) tell(error.message, true);
      }
    };

    const client = new Client({
      brokerURL: wsUrl(),
      reconnectDelay: 3000,
      connectHeaders: { login: credentials.username, passcode: credentials.password },
      debug: () => {},
      onConnect: () => {
        if (!active) return;
        setLive(true);
        subscription = client.subscribe(`/topic/tables/${tableId}`, (message) => {
          try {
            void syncTableState(JSON.parse(message.body));
          } catch (error) {
            if (active) tell(error.message || "Unable to process a table update.", true);
          }
        });
        void syncTableState();
      },
      onWebSocketClose: () => { if (active) setLive(false); },
      onStompError: () => { if (active) setLive(false); },
    });

    client.activate();
    return () => {
      active = false;
      subscription?.unsubscribe();
      setLive(false);
      void client.deactivate();
    };
  }, [sessionCredentials, sessionPlayerId, table?.tableId, tell]);
  useEffect(() => { const mine = round?.players?.find((p) => p.playerId === session?.player?.playerId); if (!round || !session || hand || mine?.visibility !== "SEEN") return; api(`/rounds/${round.roundId}/players/${session.player.playerId}/hand`, session.credentials).then(setHand).catch((e) => tell(e.message, true)); }, [round, session, hand, tell]);
  async function start() { unlockSound(); try { const next = await api(`/tables/${table.tableId}/rounds`, session.credentials, "POST", { hostPlayerId: session.player.playerId }); setRound(next); setHand(null); setShowdown(null); } catch (e) { tell(e.message, true); } }
  async function run(path, body) {
    unlockSound();
    try {
      const next = await api(path, session.credentials, "POST", body);
      const player = await api(`/players/${session.player.playerId}`, session.credentials);
      const key = `action:${next.roundId}:${next.pot}:${body?.action || body?.response || path}`;
      if (body?.action === "SAME") playSound("same", key);
      else if (body?.action === "RAISE") playSound("raise", key);
      else if (body?.action === "SHOW") playSound("bet", key);
      else if (path.endsWith("/side-shows")) playSound("sideShowRequest", key);
      setRound(next); setSession((current) => current ? { ...current, player } : current); return true;
    } catch (e) { playSound("error", `error:${path}:${Date.now()}`); tell(e.message, true); return false; }
  }
  function signOut() { setSession(null); setTable(null); setRound(null); setHand(null); setShowdown(null); }
  return <main className="app-shell"><header className="brand">ACEVERSE <small>Authoritative Teen Patti</small>{session && <span className="identity">{session.player.username} - {session.player.coinBalance} coins</span>}<button className="text-button sound-toggle" type="button" onClick={toggleSound}>{soundEnabled ? "🔊 Sound ON" : "🔇 Sound OFF"}</button></header><Message error={error} notice={notice} />{!session ? <Auth setSession={setSession} tell={tell} /> : !round ? <Lobby session={session} table={table} setTable={setTable} start={start} signOut={signOut} tell={tell} /> : <Table session={session} table={table} round={round} hand={hand} showdown={showdown} live={live} run={run} leaveRound={() => run(`/rounds/${round.roundId}/leave`)} playAgain={() => { setRound(null); setHand(null); setShowdown(null); }} />}</main>;
}
