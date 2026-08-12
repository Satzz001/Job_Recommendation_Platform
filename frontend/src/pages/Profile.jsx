import { useState, useEffect, useMemo } from "react";
import { Api } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import Avatar from "../components/Avatar.jsx";
import { useToast } from "../context/ToastContext.jsx";
import { Link } from "react-router-dom";

function DetailField({ label, value, mono }) {
  return (
    <div className="bg-cream-2 border-2 border-ink rounded-lg px-3 py-2.5">
      <div className="text-[10px] text-faint uppercase tracking-wide font-bold">{label}</div>
      <div className={`text-[13px] mt-0.5 ${mono ? "font-mono" : ""}`}>{value || "—"}</div>
    </div>
  );
}

function ProfileStat({ label, value, icon }) {
  return (
    <div className="flex items-center gap-2.5 py-2 border-b-2 border-cream-2 last:border-b-0">
      <Icon name={icon} size={16} className="text-muted" />
      <span className="text-[13px] text-muted flex-1 font-medium">{label}</span>
      <span className="font-extrabold font-mono">{value}</span>
    </div>
  );
}

export default function Profile({ user }) {
  const [mySkills, setMySkills] = useState(null);
  const [allSkills, setAllSkills] = useState([]);
  const [query, setQuery] = useState("");
  const [adding, setAdding] = useState(false);
  const toast = useToast();

  const reload = () => {
    Api.userSkills(user.id).then(setMySkills).catch(() => setMySkills([]));
    Api.allSkills().then(setAllSkills).catch(() => setAllSkills([]));
  };
  useEffect(reload, [user.id]);

  const mySet = useMemo(() => new Set((mySkills || []).map((s) => s.id)), [mySkills]);
  const suggestions = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return allSkills.filter((s) => !mySet.has(s.id) && s.name.toLowerCase().includes(q)).slice(0, 8);
  }, [query, allSkills, mySet]);

  const addSkill = async (skill) => {
    setAdding(true);
    try {
      let target = skill;
      if (!target.id) target = await Api.createSkill(skill.name);
      await Api.addUserSkill(user.id, target.id);
      toast.good(`Added ${target.name}`);
      setQuery("");
      reload();
    } catch (e) {
      toast.err(e.message || "Couldn't add skill");
    } finally {
      setAdding(false);
    }
  };

  return (
    <>
      <div className="mb-6">
        <h1 className="text-[28px] font-extrabold tracking-tight">Profile</h1>
        <p className="text-muted text-[13.5px] font-medium mt-1">Keep your skills up to date for better matches.</p>
      </div>

      <div className="grid lg:grid-cols-[minmax(0,1fr)_320px] gap-4.5">
        <div className="flex flex-col gap-4.5">
          <div className="card">
            <h3 className="font-extrabold text-base flex items-center gap-2 mb-3.5"><Icon name="tag" className="text-[var(--accent-hover)]" />Your skills</h3>

            <div className="relative mb-3.5">
              <Icon name="search" size={19} className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input className="search-input" placeholder="Add a skill (e.g. React, Postgres, Kubernetes)…" value={query} onChange={(e) => setQuery(e.target.value)}
                     onKeyDown={(e) => { if (e.key === "Enter" && query.trim()) { e.preventDefault(); addSkill(suggestions[0] || { name: query.trim() }); } }} />
              {query && (
                <div className="absolute top-[calc(100%+6px)] left-0 right-0 bg-white border-2 border-ink rounded-xl shadow-brutal p-1.5 z-10">
                  {suggestions.length > 0 ? (
                    suggestions.map((s) => (
                      <button key={s.id} className="nav-link !py-2 w-full text-left" onClick={() => addSkill(s)} disabled={adding}>
                        <Icon name="add" size={16} />{s.name}
                      </button>
                    ))
                  ) : (
                    <button className="nav-link !py-2 w-full text-left" onClick={() => addSkill({ name: query.trim() })} disabled={adding}>
                      <Icon name="add" size={16} />Create "{query.trim()}"
                    </button>
                  )}
                </div>
              )}
            </div>

            {mySkills === null && (
              <div className="flex flex-wrap gap-1.5">
                {[80, 60, 90, 70, 50].map((w, i) => <div key={i} className="skeleton-block h-[26px] rounded-full" style={{ width: w }} />)}
              </div>
            )}
            {mySkills && mySkills.length === 0 && (
              <div className="text-[13px] text-muted p-3.5 font-medium">No skills yet. Add some above or <Link to="/resume" className="font-bold">upload a resume</Link> to extract them automatically.</div>
            )}
            {mySkills && mySkills.length > 0 && (
              <div className="flex flex-wrap gap-1.5">
                {mySkills.map((s) => <span key={s.id} className="skill-chip skill-chip-matched">{s.name}</span>)}
              </div>
            )}
          </div>

          <div className="card">
            <h3 className="font-extrabold text-base flex items-center gap-2 mb-3.5"><Icon name="badge" className="text-[var(--accent-hover)]" />Account</h3>
            <div className="grid sm:grid-cols-2 gap-3">
              <DetailField label="Name" value={user.name} />
              <DetailField label="Email" value={user.email} mono />
              <DetailField label="User ID" value={`#${user.id}`} mono />
              <DetailField label="Role" value="USER" />
            </div>
          </div>
        </div>

        <aside className="flex flex-col gap-4.5">
          <div className="card text-center">
            <Avatar name={user.name} size="xl" />
            <div className="mt-3.5 font-extrabold text-base">{user.name}</div>
            <div className="text-xs text-muted font-mono">{user.email}</div>
            <div className="flex justify-center gap-2 mt-4">
              <span className="pill pill-accent"><Icon name="bolt" size={13} />Active</span>
            </div>
          </div>
          <div className="card">
            <h3 className="font-extrabold text-[13px] flex items-center gap-2 mb-3.5"><Icon name="trending_up" size={18} className="text-[var(--accent-hover)]" />Stats</h3>
            <ProfileStat label="Skills" value={(mySkills || []).length} icon="tag" />
            <ProfileStat label="In platform skill pool" value={allSkills.length} icon="database" />
          </div>
        </aside>
      </div>
    </>
  );
}
