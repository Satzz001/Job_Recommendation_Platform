export default function MatchRing({ value, size = 56 }) {
  const pct = Math.max(0, Math.min(100, Math.round(value)));
  const r = 22;
  const C = 2 * Math.PI * r;
  const off = C - (C * pct) / 100;
  return (
    <div className="relative grid place-items-center shrink-0" style={{ width: size, height: size }}>
      <svg viewBox="0 0 56 56" style={{ transform: "rotate(-90deg)", width: "100%", height: "100%" }}>
        <circle cx="28" cy="28" r={r} fill="none" strokeWidth="4" stroke="var(--accent-dim)" />
        <circle cx="28" cy="28" r={r} fill="none" strokeWidth="4" strokeLinecap="round"
                stroke="var(--accent)" strokeDasharray={C} strokeDashoffset={off}
                style={{ transition: "stroke-dashoffset 600ms" }} />
      </svg>
      <span className="absolute font-extrabold" style={{ fontSize: size < 50 ? 10 : 13 }}>
        {pct}<small className="opacity-60" style={{ fontSize: size < 50 ? 8 : 9 }}>%</small>
      </span>
    </div>
  );
}
