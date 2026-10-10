import { createContext, useContext, useMemo, useState } from "react";

const STORAGE_KEY = "jr_settings";

export const ACCENT_PRESETS = {
  purple: { name: "Purple", accent: "#8B5CF6", hover: "#7C3AED", dim: "#EDE4FD" },
  blue: { name: "Blue", accent: "#60A5FA", hover: "#3B82F6", dim: "#DBEAFE" },
  coral: { name: "Coral", accent: "#FB7185", hover: "#F43F5E", dim: "#FFE4E6" },
};

const DEFAULT_SETTINGS = {
  accent: "purple",
  density: "detailed",
  layout: "feed",
};

const SettingsContext = createContext(null);

function readStoredSettings() {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
    return { ...DEFAULT_SETTINGS, ...parsed };
  } catch {
    return DEFAULT_SETTINGS;
  }
}

function applyAccent(accentKey) {
  const preset = ACCENT_PRESETS[accentKey] || ACCENT_PRESETS.purple;
  const root = document.documentElement;
  root.style.setProperty("--accent", preset.accent);
  root.style.setProperty("--accent-hover", preset.hover);
  root.style.setProperty("--accent-dim", preset.dim);
  root.style.setProperty("--accent-border", preset.accent);
}

export function SettingsProvider({ children }) {
  const [settings, setSettings] = useState(() => {
    const next = readStoredSettings();
    applyAccent(next.accent);
    return next;
  });

  const set = (key, value) => {
    setSettings((prev) => {
      const next = { ...prev, [key]: value };
      if (key === "accent") applyAccent(value);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  };

  const value = useMemo(() => ({ settings, set }), [settings]);

  return <SettingsContext.Provider value={value}>{children}</SettingsContext.Provider>;
}

export function useSettings() {
  const value = useContext(SettingsContext);
  if (!value) throw new Error("useSettings must be used within SettingsProvider");
  return value;
}
