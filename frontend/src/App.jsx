import { useState, useCallback } from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import { Api } from "./lib/api.js";
import { useAuth } from "./context/AuthContext.jsx";
import { useToast } from "./context/ToastContext.jsx";
import { LoginPage, RegisterPage } from "./pages/Auth.jsx";
import AppShell from "./layouts/AppShell.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Jobs from "./pages/Jobs.jsx";
import Activity from "./pages/Activity.jsx";
import Resume from "./pages/Resume.jsx";
import Profile from "./pages/Profile.jsx";
import Chat from "./pages/Chat.jsx";
import Landing from "./pages/Landing.jsx";

const PAGE_TITLES = { "/dashboard": "Dashboard", "/jobs": "Browse jobs", "/activity": "Your activity", "/resume": "Resume", "/profile": "Profile", "/chat": "Ask AI" };

function RequireAuth({ children }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" state={{ from: location }} replace />;
  return children;
}

export default function App() {
  const { user } = useAuth();
  const toast = useToast();
  const [savedIds, setSavedIds] = useState(() => new Set(JSON.parse(localStorage.getItem("jr_saved") || "[]")));
  const [appliedIds, setAppliedIds] = useState(() => new Set(JSON.parse(localStorage.getItem("jr_applied") || "[]")));
  const location = useLocation();

  const persistSaved = (set) => localStorage.setItem("jr_saved", JSON.stringify([...set]));
  const persistApplied = (set) => localStorage.setItem("jr_applied", JSON.stringify([...set]));

  const handleInteract = useCallback(async (jobId, type) => {
    if (type === "SAVED") {
      setSavedIds((prev) => {
        const next = new Set(prev);
        if (next.has(jobId)) { next.delete(jobId); toast.info("Removed from saved"); }
        else { next.add(jobId); toast.good("Saved for later"); }
        persistSaved(next);
        return next;
      });
    } else if (type === "APPLIED") {
      setAppliedIds((prev) => {
        const next = new Set(prev);
        next.add(jobId);
        persistApplied(next);
        toast.good("Marked as applied");
        return next;
      });
    }
    if (user) {
      try { await Api.interact(user.id, jobId, type); } catch { /* offline-friendly */ }
    }
  }, [user, toast]);

  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <LoginPage />} />
      <Route path="/register" element={user ? <Navigate to="/dashboard" /> : <RegisterPage />} />

      <Route element={
        <RequireAuth>
          <AppShell savedCount={savedIds.size} appliedCount={appliedIds.size} pageTitle={PAGE_TITLES[location.pathname] || "Dashboard"} />
        </RequireAuth>
      }>
        <Route path="/dashboard" element={<Dashboard user={user} savedIds={savedIds} appliedIds={appliedIds} onInteract={handleInteract} />} />
        <Route path="/jobs" element={<Jobs savedIds={savedIds} appliedIds={appliedIds} onInteract={handleInteract} />} />
        <Route path="/activity" element={<Activity savedIds={savedIds} appliedIds={appliedIds} onInteract={handleInteract} />} />
        <Route path="/resume" element={<Resume user={user} />} />
        <Route path="/profile" element={<Profile user={user} />} />
        <Route path="/chat" element={<Chat user={user} />} />
      </Route>

      <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
    </Routes>
  );
}
