import { useState, useRef, useEffect } from "react";
import Icon from "./Icon.jsx";

export default function FilterPill({ label, icon, options, value, onChange }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const fn = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", fn);
    return () => document.removeEventListener("mousedown", fn);
  }, []);

  const current = options.find((o) => o.value === value);

  return (
    <div className="relative" ref={ref}>
      <button className={`filter-pill ${value !== "ALL" ? "filter-pill-active" : ""}`} onClick={() => setOpen((o) => !o)}>
        <Icon name={icon} size={16} />
        {value === "ALL" ? label : current?.label || label}
        <Icon name="expand_more" size={16} />
      </button>
      {open && (
        <div className="absolute top-[calc(100%+8px)] z-20 bg-white border-2 border-ink rounded-xl shadow-brutal p-2 min-w-[180px] flex flex-col gap-0.5">
          {options.map((o) => (
            <button
              key={o.value}
              className={`text-left px-2.5 py-2 rounded-lg text-[13px] font-semibold hover:bg-cream-2 ${value === o.value ? "!bg-[var(--accent-dim)]" : ""}`}
              onClick={() => { onChange(o.value); setOpen(false); }}
            >
              {o.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
