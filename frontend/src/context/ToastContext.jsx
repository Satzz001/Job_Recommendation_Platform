import { createContext, useContext, useMemo, useState } from "react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const push = (kind, message) => {
    const id = `${Date.now()}-${Math.random()}`;
    setToasts((prev) => [...prev, { id, kind, message }]);
    window.setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 2800);
  };

  const value = useMemo(
    () => ({
      good: (message) => push("good", message),
      info: (message) => push("info", message),
      warn: (message) => push("warn", message),
    }),
    [],
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="fixed right-4 bottom-4 z-[120] flex flex-col gap-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`max-w-[300px] border-2 border-ink rounded-lg px-3 py-2 text-[12.5px] font-semibold shadow-brutal-sm ${
              toast.kind === "good"
                ? "bg-green-100 text-green-700"
                : toast.kind === "warn"
                  ? "bg-amber-100 text-amber-700"
                  : "bg-white text-ink"
            }`}
            role="status"
          >
            {toast.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const value = useContext(ToastContext);
  if (!value) throw new Error("useToast must be used within ToastProvider");
  return value;
}
