import { NavLink as RouterNavLink, Outlet, useNavigate } from "react-router-dom";
import { useState } from "react";
import Icon from "../components/Icon.jsx";
import Avatar from "../components/Avatar.jsx";
import SettingsPanel from "../components/SettingsPanel.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { useToast } from "../context/ToastContext.jsx";

function NavItem({ to, icon, children, badge }) {
  return (
    <RouterNavLink to={to} className={({ isActive }) => `nav-link ${isActive ? "nav-link-active" : ""}`}>
      {({ isActive }) => (
        <>
          <Icon name={icon} filled={isActive} size={20} />
          <span>{children}</span>
          {badge ? <span className="ml-auto bg-ink text-cream text-[10.5px] font-bold px-1.5 py-0.5 rounded-full">{badge}</span> : null}
        </>
      )}
    </RouterNavLink>
  );
}

export default function AppShell({ savedCount, appliedCount, pageTitle }) {
  const { user, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const doLogout = () => {
    logout();
    toast.info("Signed out");
    navigate("/login");
  };

  return (
    <div className="min-h-screen grid grid-cols-1 md:grid-cols-[248px_1fr]">
      {mobileOpen && <div className="fixed inset-0 z-[75] bg-ink/30 md:hidden" onClick={() => setMobileOpen(false)} />}
      <aside className={`fixed md:sticky top-0 left-0 h-screen w-[264px] md:w-auto z-[80] bg-cream border-r-2 border-ink flex flex-col p-6 gap-1 transition-transform md:translate-x-0 ${mobileOpen ? "translate-x-0 shadow-brutal-lg" : "-translate-x-full"}`}>
        <div className="flex items-center gap-2 px-2 pb-6 font-extrabold text-lg">
          <div className="w-[30px] h-[30px] rounded-lg border-2 border-ink grid place-items-center font-extrabold text-sm shadow-brutal-sm" style={{ background: "var(--accent)", color: "var(--accent-contrast)" }}>jr</div>
          <span>jobrecomm</span>
        </div>

        <div className="text-[10.5px] uppercase tracking-wide text-faint font-bold px-2.5 pt-3.5 pb-1.5">Discover</div>
        <NavItem to="/dashboard" icon="auto_awesome">For you</NavItem>
        <NavItem to="/jobs" icon="search">All jobs</NavItem>

        <div className="text-[10.5px] uppercase tracking-wide text-faint font-bold px-2.5 pt-3.5 pb-1.5">Activity</div>
        <NavItem to="/activity" icon="bookmark" badge={(savedCount + appliedCount) || null}>Saved & applied</NavItem>

        <div className="text-[10.5px] uppercase tracking-wide text-faint font-bold px-2.5 pt-3.5 pb-1.5">You</div>
        <NavItem to="/resume" icon="upload_file">Resume</NavItem>
        <NavItem to="/profile" icon="account_circle">Profile</NavItem>
        <NavItem to="/chat" icon="forum">Ask AI</NavItem>

        <button className="mt-auto flex items-center gap-2.5 p-2.5 rounded-xl bg-white border-2 border-ink shadow-brutal-sm hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-brutal transition-transform text-left" onClick={doLogout} title="Sign out">
          <Avatar name={user?.name} />
          <div className="min-w-0 flex-1">
            <div className="text-[13.5px] font-bold truncate">{user?.name}</div>
            <div className="text-[11px] text-faint truncate">{user?.email}</div>
          </div>
          <Icon name="logout" size={16} className="text-faint" />
        </button>
      </aside>

      <main className="flex flex-col min-w-0">
        <header className="sticky top-0 z-[5] flex items-center gap-4 px-7 py-4.5 bg-cream">
          <button className="btn btn-icon btn-ghost md:hidden" onClick={() => setMobileOpen(true)} aria-label="Menu"><Icon name="menu" /></button>
          <div className="hidden md:block text-sm font-medium text-muted">
            <span className="text-faint">jobrecomm</span> · <strong className="text-ink font-extrabold">{pageTitle}</strong>
          </div>
          <div className="relative flex-1 max-w-[520px]">
            <Icon name="search" size={19} className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input className="search-input" placeholder="Search jobs, skills, companies…" onFocus={() => navigate("/jobs")} />
            <kbd className="absolute right-3 top-1/2 -translate-y-1/2 text-[10px] font-bold px-1.5 py-0.5 rounded-md bg-cream-2 border border-ink">⌘K</kbd>
          </div>
        </header>
        <div className="px-4 md:px-8 pt-3 md:pt-2 pb-20 max-w-[1320px] w-full mx-auto">
          <Outlet />
        </div>
      </main>

      <SettingsPanel />
    </div>
  );
}
