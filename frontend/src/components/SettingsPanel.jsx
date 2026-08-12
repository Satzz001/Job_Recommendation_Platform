import { useState, useRef, useEffect } from "react";
import Icon from "./Icon.jsx";
import { useSettings, ACCENT_PRESETS } from "../context/SettingsContext.jsx";

function Radio({ label, value, options, onChange }) {
  return (
    <div className="mb-4">
      <div className="field-label mb-1.5">{label}</div>
      <div className="flex gap-1.5 flex-wrap">
        {options.map((o) => (
          <button
            key={o.value}
            onClick={() => onChange(o.value)}
            className={`px-3 py-1.5 rounded-lg border-2 border-ink text-xs font-bold ${value === o.value ? "bg-[var(--accent-dim)]" : "bg-white"}`}
          >
            {o.label}
          </button>
        ))}
      </div>
    </div>
  );
}

export default function SettingsPanel() {
  const [open, setOpen] = useState(false);
  const { settings, set } = useSettings();
  const ref = useRef(null);

  useEffect(() => {
    const fn = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", fn);
    return () => document.removeEventListener("mousedown", fn);
  }, []);

  return (
    <div className="fixed bottom-5 right-5 z-[90]" ref={ref}>
      {open && (
        <div className="card !shadow-brutal mb-3 w-72">
          <div className="font-extrabold text-sm mb-4">Appearance</div>
          <div className="mb-4">
            <div className="field-label mb-1.5">Accent</div>
            <div className="flex gap-2">
              {Object.entries(ACCENT_PRESETS).map(([key, p]) => (
                <button
                  key={key}
                  title={p.name}
                  onClick={() => set("accent", key)}
                  className="w-8 h-8 rounded-lg border-2 border-ink"
                  style={{ background: p.accent, boxShadow: settings.accent === key ? `0 0 0 2px #FCF3E1, 0 0 0 4px ${p.accent}` : "none" }}
                />
              ))}
            </div>
          </div>
          <Radio label="Job card style" value={settings.density} onChange={(v) => set("density", v)}
                 options={[{ value: "detailed", label: "Detailed" }, { value: "compact", label: "Compact" }, { value: "minimal", label: "Minimal" }]} />
          <Radio label="Dashboard layout" value={settings.layout} onChange={(v) => set("layout", v)}
                 options={[{ value: "feed", label: "Feed" }, { value: "grid", label: "Grid" }]} />
        </div>
      )}
      <button className="btn btn-icon !rounded-full !p-3" onClick={() => setOpen((o) => !o)} aria-label="Appearance settings">
        <Icon name={open ? "close" : "tune"} />
      </button>
    </div>
  );
}
