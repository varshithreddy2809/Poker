export default function GamePot({ pot, currentBet, contribution, balance }) {
  return <div className="poker-table__pot"><span>Current pot</span><strong>{pot} coins</strong><small>Required bet {currentBet}</small><small>Your contribution {contribution ?? 0}</small><small>Your balance {balance} coins</small></div>;
}
