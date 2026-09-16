export default function StatCard({ value, label, detail }) {
  return <article className="stat-card"><strong className="stat-card__value">{value}</strong><h3>{label}</h3><p>{detail}</p></article>;
}
