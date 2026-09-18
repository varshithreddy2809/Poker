/* eslint-disable no-irregular-whitespace -- legacy landing copy retains full-width separators. */
import { Client } from "@stomp/stompjs";
import { useCallback, useEffect, useState } from "react";
import CinematicBackground from "./components/CinematicBackground/CinematicBackground.jsx";
import CasinoDecorations from "./components/CasinoDecorations/CasinoDecorations.jsx";
import HomePage from "./components/HomePage/HomePage.jsx";
import GamesPage from "./components/GamesPage/GamesPage.jsx";
import Navbar from "./components/Navbar/Navbar.jsx";
import AuthForm from "./components/AuthForm/AuthForm.jsx";
import LobbyPage from "./components/LobbyPage/LobbyPage.jsx";
import GameTable from "./components/GameTable/GameTable.jsx";

const API = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");

function auth(credentials) { return `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`; }
function wsUrl() { if (import.meta.env.VITE_WS_URL) return import.meta.env.VITE_WS_URL; return `${location.protocol === "https:" ? "wss" : "ws"}://${location.hostname}:8080/ws`; }
async function api(path, credentials, method = "GET", body) {
  const headers = { Accept: "application/json" }; if (credentials) headers.Authorization = auth(credentials); if (body !== undefined) headers["Content-Type"] = "application/json";
  const response = await fetch(`${API}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const payload = await response.json().catch(() => ({})); if (!response.ok) throw new Error(payload.message || `Request failed (${response.status})`); return payload;
}
function Message({ error, notice }) { return (error || notice) && <div className={`message ${error ? "error" : "notice"}`}>{error || notice}</div>; }

function Auth({ setSession, tell, defaultMode }) {
  const [register, setRegister] = useState(defaultMode === "register"); const [showForm, setShowForm] = useState(Boolean(defaultMode)); const [form, setForm] = useState({ username: "", firstName: "", lastName: "", email: "", phone: "", age: "", gender: "", plan: "Standard", password: "" }); const [busy, setBusy] = useState(false);
  const update = (name) => (event) => setForm({ ...form, [name]: event.target.value });
  async function submit(event) {
    event.preventDefault(); setBusy(true);
    try {
      const credentials = { username: form.username.trim(), password: form.password }; let player;
      if (register) player = await api("/players", null, "POST", { username: credentials.username, email: form.email, password: form.password });
      else { const id = sessionStorage.getItem(`aceverse:${credentials.username}`); if (!id) throw new Error("Register this player in this browser before signing in."); player = await api(`/players/${id}`, credentials); }
      sessionStorage.setItem(`aceverse:${player.username}`, player.playerId); setSession({ credentials, player }); tell(`Welcome ${player.username}.`);
    } catch (error) { tell(error.message, true); } finally { setBusy(false); }
  }
  if (showForm) return <section className="auth-scene"><section className="panel auth-panel"><button className="auth-back" type="button" onClick={() => setShowForm(false)}>Back</button><div className="panel-crown" aria-hidden="true">♛</div><p className="eyebrow">Private table access</p><h1>ACEVERSE</h1><div className="gold-rule" aria-hidden="true" /><AuthForm register={register} setRegister={setRegister} form={form} update={update} busy={busy} onSubmit={submit} /></section></section>;
  if (showForm) return <section className="auth-scene"><div className="scene-card scene-card-left" aria-hidden="true"><span>A</span><b>♠</b><i>♠</i></div><div className="scene-card scene-card-right" aria-hidden="true"><span>A</span><b>♥</b><i>♥</i></div><section className="panel auth-panel"><button className="auth-back" onClick={() => setShowForm(false)}>← Back</button><div className="panel-crown" aria-hidden="true">♛</div><p className="eyebrow">Private table access</p><h1>ACEVERSE</h1><div className="gold-rule" aria-hidden="true" /><div className="tabs"><button className={!register ? "selected" : ""} onClick={() => setRegister(false)}>Sign in</button><button className={register ? "selected" : ""} onClick={() => setRegister(true)}>Register</button></div><form className="form-grid" onSubmit={submit}><label>Username<input minLength="3" value={form.username} onChange={update("username")} required /></label>{register && <label>Email<input type="email" value={form.email} onChange={update("email")} required /></label>}<label>Password<input type="password" minLength="8" value={form.password} onChange={update("password")} required /></label><button className="primary wide" disabled={busy}>{register ? "Create player" : "Enter lobby"}</button></form></section></section>;
  return <section className="landing"><div className="landing-card landing-card-left" aria-hidden="true">A<span>♠</span></div><div className="landing-card landing-card-right" aria-hidden="true">A<span>♥</span></div><div className="landing-chips left" aria-hidden="true" /><div className="landing-chips right" aria-hidden="true" /><aside className="landing-slogan">SAME CARDS<br />DIFFERENT PEOPLE<br />INFINITE STORIES</aside><aside className="landing-stats"><p>◉ <b>10K+</b><span>Active Players</span></p><p>◉ <b>Multiple</b><span>Card Games</span></p><p>◉ <b>100% Fair</b><span>Play System</span></p><p>◉ <b>Play Anytime</b><span>Anywhere</span></p></aside><section className="landing-main"><div className="landing-crown">♛</div><p className="welcome">WELCOME TO</p><h1>ACEVERSE</h1><p className="landing-tagline">MORE GAMES　•　MORE PEOPLE　•　A BIGGER TABLE</p><div className="landing-rule" /><p className="landing-subtitle">A Premium Online Card Game Platform</p><p className="landing-copy">Create your account, join a room, challenge your friends,<br />play your favorite card games, and experience the thrill — all in one place.<br />Real Players. Real Fun. Always On.</p><div className="landing-actions"><button className="landing-login" onClick={() => { setRegister(false); setShowForm(true); }}>⇥ <span>LOGIN<small>Welcome Back</small></span></button><button className="landing-register" onClick={() => { setRegister(true); setShowForm(true); }}>♟ <span>CREATE ACCOUNT<small>Join the Table</small></span></button></div><div className="feature-title">GAME FEATURES</div><div className="landing-features">{[["♟","Multiplayer","Up to 8 Players"],["◉","Virtual Coins","Fair Play Always"],["◉","Blind & See","Play Your Way"],["↗","Raise, Call & Fold","Full Control"],["♢","Side Show","Challenge Anywhere"],["♜","Live Results","Real-Time"],["▥","Game History","Track Your Journey"],["♛","Secure & Safe","Play with Trust"]].map(([icon, title, note]) => <div className="landing-feature" key={title}><b>{icon}</b><strong>{title}</strong><small>{note}</small></div>)}</div><div className="landing-bottom">IT'S NOT JUST A GAME, IT'S A COMMUNITY</div></section></section>;
}

function Lobby({ session, table, setTable, start, signOut, tell }) {
  const [discoveredTable, setDiscoveredTable] = useState(null);
  async function create({ name, bet, max }) { try { setTable(await api("/tables", session.credentials, "POST", { hostPlayerId: session.player.playerId, tableName: name, entryBet: Number(bet), maxPlayers: Number(max) })); } catch (error) { tell(error.message, true); } }
  async function lookup(tableId) { try { setDiscoveredTable(await api(`/tables/${tableId}`, session.credentials)); } catch (error) { tell(error.message, true); } }
  async function join(tableId) { try { setTable(await api(`/tables/${tableId}/players`, session.credentials, "POST", { playerId: session.player.playerId })); } catch (error) { tell(error.message, true); } }
  async function refresh() { try { if (table) setTable(await api(`/tables/${table.tableId}`, session.credentials)); else if (discoveredTable) setDiscoveredTable(await api(`/tables/${discoveredTable.tableId}`, session.credentials)); } catch (error) { tell(error.message, true); } }
  return <LobbyPage session={session} table={table} discoveredTable={discoveredTable} onCreate={create} onLookup={lookup} onJoin={join} onRefresh={refresh} onStart={start} onSignOut={signOut} />;
}

/* Previous table presentation retained for reference.
function LegacyTable({ session, table, round, hand, showdown, live, run, leaveRound, playAgain }) {
  const mine = round.players?.find((p) => p.playerId === session.player.playerId); const myTurn = round.currentTurnPlayerId === session.player.playerId; const [raise, setRaise] = useState(round.currentBet + 1); const [target, setTarget] = useState("");
  const players = showdown?.hands || round.players || []; const targets = (round.players || []).filter((p) => p.status === "ACTIVE" && p.playerId !== session.player.playerId);
  return <section className="game-board"><header className="game-header"><div><p className="eyebrow">Round {round.roundNumber} - pot {round.pot} - bet {round.currentBet}</p><h2>{table.tableName}</h2></div><div className="live-status"><span className={live ? "live-dot connected" : "live-dot"} />{live ? "LIVE" : "RECONNECTING"}</div></header>
    {!showdown && <div className="round-message">{round.phase === "CHOOSING_VISIBILITY" ? "Choose Blind or Seen. After 30 seconds unselected players become Blind." : round.phase === "FORCED_SAME" ? `Rejected Side Show: SAME only, ${round.forcedSameTurnsRemaining} total turns remaining (${Math.ceil(round.forcedSameTurnsRemaining / 2)} each).` : myTurn ? "Your turn. The server will auto-drop you after 30 seconds." : "Waiting for the current player's action."}</div>}
    <div className="players-grid">{players.map((p) => { const own = p.playerId === session.player.playerId; const cards = showdown ? p.cards || [] : own ? hand?.cards || [] : [1, 2, 3]; return <article className={`player-hand ${showdown?.winnerPlayerId === p.playerId ? "winner" : ""}`} key={p.playerId}><div className="player-name">{p.username}{own && " (you)"}</div><div className="cards">{cards.map((c, i) => <Card key={c.position || i} card={c} hidden={!showdown && !own} />)}</div><div className="category">{showdown ? p.handCategory?.replaceAll("_", " ") : `${p.visibility} - ${p.status}`}</div></article>; })}</div>
    {!showdown && mine?.visibility === "PENDING" && <div className="actions centered"><button className="primary" onClick={() => run(`/rounds/${round.roundId}/visibility`, { choice: "BLIND" })}>Play Blind</button><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/visibility`, { choice: "SEEN" })}>See Cards</button></div>}
    {!showdown && mine?.visibility === "BLIND" && <div className="actions centered"><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/see-cards`)}>See my cards</button></div>}
    {!showdown && myTurn && ["BETTING", "FORCED_SAME"].includes(round.phase) && <div className="actions centered"><button className="primary" onClick={() => run(`/rounds/${round.roundId}/actions`, { action: "SAME" })}>Same {round.currentBet}</button>{round.phase === "BETTING" && <><input className="raise-input" type="number" min={round.currentBet + 1} value={raise} onChange={(e) => setRaise(e.target.value)} /><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/actions`, { action: "RAISE", amount: Number(raise) })}>Raise</button><select value={target} onChange={(e) => setTarget(e.target.value)}><option value="">Side Show player</option>{targets.map((p) => <option key={p.playerId} value={p.playerId}>{p.username}</option>)}</select><button className="secondary" disabled={!target} onClick={() => run(`/rounds/${round.roundId}/side-shows`, { targetPlayerId: Number(target) })}>Side Show</button></>}<button className="text-button" onClick={leaveRound}>Drop</button></div>}
    {!showdown && round.phase === "SIDE_SHOW_PENDING" && round.pendingSideShowTargetPlayerId === session.player.playerId && <div className="actions centered"><button className="primary" onClick={() => run(`/rounds/${round.roundId}/side-shows/${round.pendingSideShowId}/response`, { response: "ACCEPT" })}>Accept Side Show</button><button className="secondary" onClick={() => run(`/rounds/${round.roundId}/side-shows/${round.pendingSideShowId}/response`, { response: "REJECT" })}>Reject</button></div>}
    {!showdown && mine?.status === "ACTIVE" && !myTurn && <div className="actions centered"><button className="text-button" onClick={leaveRound}>Leave game / Drop</button></div>}
    {showdown && <div className="round-message">{showdown.winnerUsername} wins: {showdown.winningHandCategory.replaceAll("_", " ")}</div>}{showdown && table.hostPlayerId === session.player.playerId && <div className="actions centered"><button className="primary" onClick={playAgain}>Play again</button></div>}
  </section>;
}
*/
function Table({ session, table, round, hand, showdown, live, run, leaveRound, playAgain, borrowing, borrow, refreshBorrowing }) {
  return <GameTable session={session} table={table} round={round} hand={hand} showdown={showdown} live={live} run={run} leaveRound={leaveRound} playAgain={playAgain} borrowing={borrowing} borrow={borrow} refreshBorrowing={refreshBorrowing} />;
}

export default function Game() {
  const [session, setSession] = useState(null); const [table, setTable] = useState(null); const [round, setRound] = useState(null); const [hand, setHand] = useState(null); const [showdown, setShowdown] = useState(null); const [live, setLive] = useState(false); const [borrowing, setBorrowing] = useState(null); const [error, setError] = useState(""); const [notice, setNotice] = useState(""); const [authRequest, setAuthRequest] = useState(null); const [page, setPage] = useState(() => location.pathname === "/games" ? "games" : "home");
  const tell = useCallback((text, isError = false) => { setError(isError ? text : ""); setNotice(isError ? "" : text); }, []);
  const refreshTable = useCallback(async (id, credentials) => setTable(await api(`/tables/${id}`, credentials)), []);
  const refreshBorrowing = useCallback(async () => {
    if (!session) return;
    setBorrowing(await api("/coin-borrowing", session.credentials));
  }, [session]);
  useEffect(() => { if (!session || !table?.tableId) return; const client = new Client({ brokerURL: wsUrl(), reconnectDelay: 3000, debug: () => {}, connectHeaders: { login: session.credentials.username, passcode: session.credentials.password }, onConnect: () => { setLive(true); client.subscribe(`/topic/tables/${table.tableId}`, async (message) => { const event = JSON.parse(message.body); try { if (event.type === "SHOWDOWN") { setShowdown(event.showdown); await refreshTable(table.tableId, session.credentials); } else if (event.round) { setRound(event.round); setShowdown(null); setHand(null); await refreshTable(table.tableId, session.credentials); } else await refreshTable(table.tableId, session.credentials); if (event.type === "BORROWING_UPDATED") await refreshBorrowing(); } catch (e) { tell(e.message, true); } }); }, onWebSocketClose: () => setLive(false) }); client.activate(); return () => { setLive(false); client.deactivate(); }; }, [session, table?.tableId, refreshTable, refreshBorrowing, tell]);
  useEffect(() => { if (!session || !round) return undefined; const timer = window.setTimeout(() => { refreshBorrowing().catch((e) => tell(e.message, true)); }, 0); return () => window.clearTimeout(timer); }, [session, round, refreshBorrowing, tell]);
  useEffect(() => { const mine = round?.players?.find((p) => p.playerId === session?.player?.playerId); if (!round || !session || hand || mine?.visibility !== "SEEN") return; api(`/rounds/${round.roundId}/players/${session.player.playerId}/hand`, session.credentials).then(setHand).catch((e) => tell(e.message, true)); }, [round, session, hand, tell]);
  async function start() { try { const next = await api(`/tables/${table.tableId}/rounds`, session.credentials, "POST", { hostPlayerId: session.player.playerId }); setRound(next); setHand(null); setShowdown(null); } catch (e) { tell(e.message, true); } }
  async function refreshMyBalance() {
    const player = await api(`/players/${session.player.playerId}`, session.credentials);
    setSession((current) => ({ ...current, player }));
  }
  async function run(path, body) {
    try {
      const next = await api(path, session.credentials, "POST", body);
      setRound(next);
      await refreshMyBalance();
      return true;
    } catch (e) { tell(e.message, true); return false; }
  }
  async function borrow(path, body, message) {
    try { await api(path, session.credentials, "POST", body); await Promise.all([refreshMyBalance(), refreshBorrowing()]); tell(message); return true; } catch (e) { tell(e.message, true); return false; }
  }
  function signOut() { setSession(null); setTable(null); setRound(null); setHand(null); setShowdown(null); setBorrowing(null); }
  const openAuth = (mode) => setAuthRequest({ mode, requestedAt: Date.now() });
  useEffect(() => { const syncPage = () => setPage(location.pathname === "/games" ? "games" : "home"); window.addEventListener("popstate", syncPage); return () => window.removeEventListener("popstate", syncPage); }, []);
  function navigate(item) {
    if (item === "Games" || item === "Home") { const next = item === "Games" ? "games" : "home"; const path = next === "games" ? "/games" : "/"; if (location.pathname !== path) history.pushState({}, "", path); setPage(next); window.scrollTo({ top: 0, behavior: "smooth" }); return; }
    if (page !== "home") { history.pushState({}, "", "/"); setPage("home"); }
    window.setTimeout(() => document.getElementById(item === "Features" ? "features" : item === "How to Play" || item === "About" ? "statistics" : "footer")?.scrollIntoView({ behavior: "smooth" }), 0);
  }
  return <main className="app-shell"><CinematicBackground />{!session && <CasinoDecorations />}{!session ? <><Navbar onLogin={() => openAuth("login")} onRegister={() => openAuth("register")} onNavigate={navigate} activeItem={page === "games" ? "Games" : "Home"} />{page === "games" ? <GamesPage onPlayTeenPatti={() => openAuth("login")} /> : <HomePage onLogin={() => openAuth("login")} onRegister={() => openAuth("register")} />}</> : <header className="brand">ACEVERSE <small>Authoritative Teen Patti</small><span className="identity">{session.player.username} - {session.player.coinBalance} coins</span></header>}<Message error={error} notice={notice} />{!session ? <Auth key={authRequest?.requestedAt ?? "landing"} setSession={setSession} tell={tell} defaultMode={authRequest?.mode} /> : !round ? <Lobby session={session} table={table} setTable={setTable} start={start} signOut={signOut} tell={tell} /> : <Table session={session} table={table} round={round} hand={hand} showdown={showdown} live={live} run={run} leaveRound={() => run(`/rounds/${round.roundId}/leave`)} playAgain={() => { setRound(null); setHand(null); setShowdown(null); }} borrowing={borrowing} borrow={borrow} refreshBorrowing={refreshBorrowing} />}</main>;
}
