export default function Empty({ icon = "inbox", title, subtitle, action }) {
  return (
    <div className="py-16 px-6 text-center text-muted">
      <span className="material-symbols-rounded text-4xl text-faint mb-3 block">{icon}</span>
      <div className="font-extrabold text-ink mb-1">{title}</div>
      <div>{subtitle}</div>
      {action && <div className="mt-3.5">{action}</div>}
    </div>
  );
}
