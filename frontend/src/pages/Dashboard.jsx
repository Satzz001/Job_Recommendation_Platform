import { useState, useEffect, useMemo } from "react";
import { Api } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import JobCard from "../components/JobCard.jsx";
import { JobCardSkeleton } from "../components/JobCardSkeleton.jsx";
import Empty from "../components/Empty.jsx";
import JobDetailDrawer from "../components/JobDetailDrawer.jsx";
import { useSettings } from "../context/SettingsContext.jsx";
import { Link } from "react-router-dom";

export default function Dashboard({ user, savedIds, appliedIds, onInteract }) {
  const { settings } = useSettings();
  const [recs, setRecs] = useState(null);
  const [err, setErr] = useState(null);
  const [open, setOpen] = useState(null);
  const [skills, setSkills] = useState([]);

  useEffect(() => {
    let alive = true;
    Api.recommendations(user.id, 0, 12)
      .then((p) => alive && setRecs(p.content || p || []))
      .catch((e) => alive && setErr(e.message));
    Api.userSkills(user.id).then((s) => alive && setSkills(s || [])).catch(() => {});
    return () => { alive = false; };
  }, [user.id]);

  const greeting = useMemo(() => {
    const h = new Date().getHours();
    return h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening";
  }, []);

  const topMatch = recs && recs.length ? Math.round((recs[0].matchScore || 0) * 100) : null;
  const gridClass = settings.layout === "grid" ? "grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4" : "flex flex-col gap-4";

  return (
    <>
      <div className="relative rounded-xl2 border-2 border-ink shadow-brutal p-7 md:p-8 mb-7" style={{ background: "var(--accent-dim)" }}>
        <div className="inline-flex items-center gap-1.5 text-[11.5px] font-extrabold uppercase tracking-wide mb-2.5" style={{ color: "var(--accent-hover)" }}>
          <span className="w-1.5 h-1.5 rounded-full border border-ink animate-pulse" style={{ background: "var(--accent)" }} /> AI recommendations · live
        </div>
        <h1 className="text-2xl md:text-[26px] font-extrabold tracking-tight mb-1.5">{greeting}, {user.name?.split(" ")[0] || "there"}.</h1>
        <p className="opacity-75 mb-5 max-w-[560px] text-[13.5px] font-medium">
          Your top matches today, ranked by how well your skills line up with each role. Update your resume or add skills to sharpen the list.
        </p>
        <div className="flex gap-9 flex-wrap mt-5">
          <Stat label="Top match" value={topMatch !== null ? `${topMatch}` : "—"} suffix={topMatch !== null ? "%" : ""} />
          <Stat label="Recommendations" value={recs?.length ?? "—"} />
          <Stat label="Your skills" value={skills.length} />
          <Stat label="Applied" value={appliedIds.size} />
        </div>
      </div>

      <section className="mb-8">
        <div className="flex items-center justify-between mb-4 gap-3">
          <h2 className="font-extrabold text-base flex items-center gap-2"><Icon name="auto_awesome" className="text-[var(--accent-hover)]" size={20} />Matched for you</h2>
          <Link to="/jobs" className="btn btn-sm btn-ghost">View all<Icon name="arrow_forward" size={16} /></Link>
        </div>

        {err && <Empty icon="cloud_off" title="Couldn't load recommendations" subtitle={err} action={<button className="btn" onClick={() => location.reload()}><Icon name="refresh" />Retry</button>} />}
        {!err && recs === null && <div className={gridClass}>{[0, 1, 2].map((i) => <JobCardSkeleton key={i} />)}</div>}
        {!err && recs && recs.length === 0 && (
          <Empty icon="rocket_launch" title="No recommendations yet" subtitle="Upload a resume or add a few skills to your profile to seed your matches."
                 action={<Link to="/resume" className="btn btn-primary"><Icon name="upload_file" />Upload resume</Link>} />
        )}
        {!err && recs && recs.length > 0 && (
          <div className={gridClass}>
            {recs.map((j) => (
              <JobCard key={j.id} job={j} variant={settings.density === "compact" ? "compact" : settings.density === "minimal" ? "minimal" : "detailed"}
                       saved={savedIds.has(j.id)} applied={appliedIds.has(j.id)}
                       onOpen={() => { setOpen(j); onInteract(j.id, "CLICKED"); }}
                       onSave={() => onInteract(j.id, "SAVED")} onApply={() => onInteract(j.id, "APPLIED")} />
            ))}
          </div>
        )}
      </section>

      {open && (
        <JobDetailDrawer job={open} onClose={() => setOpen(null)} saved={savedIds.has(open.id)} applied={appliedIds.has(open.id)}
                          onSave={() => onInteract(open.id, "SAVED")} onApply={() => onInteract(open.id, "APPLIED")} />
      )}
    </>
  );
}

function Stat({ label, value, suffix }) {
  return (
    <div>
      <div className="text-[11px] opacity-70 uppercase tracking-wide font-bold mb-1">{label}</div>
      <div className="text-2xl font-extrabold tracking-tight">{value}{suffix && <small className="text-[13px] font-semibold">{suffix}</small>}</div>
    </div>
  );
}
