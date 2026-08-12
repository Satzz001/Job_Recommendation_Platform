import { useState, useEffect, useMemo } from "react";
import { Api } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import JobCard from "../components/JobCard.jsx";
import { JobCardSkeleton } from "../components/JobCardSkeleton.jsx";
import Empty from "../components/Empty.jsx";
import JobDetailDrawer from "../components/JobDetailDrawer.jsx";
import FilterPill from "../components/FilterPill.jsx";
import { useSettings } from "../context/SettingsContext.jsx";

export default function Jobs({ savedIds, appliedIds, onInteract }) {
  const { settings } = useSettings();
  const [jobs, setJobs] = useState(null);
  const [err, setErr] = useState(null);
  const [q, setQ] = useState("");
  const [type, setType] = useState("ALL");
  const [exp, setExp] = useState("ALL");
  const [loc, setLoc] = useState("ALL");
  const [open, setOpen] = useState(null);

  useEffect(() => {
    let alive = true;
    Api.allJobs().then((j) => alive && setJobs(j)).catch((e) => alive && setErr(e.message));
    return () => { alive = false; };
  }, []);

  const locations = useMemo(() => (jobs ? [...new Set(jobs.map((j) => j.location).filter(Boolean))].sort() : []), [jobs]);

  const filtered = useMemo(() => {
    if (!jobs) return null;
    const qq = q.trim().toLowerCase();
    return jobs.filter((j) => {
      if (type !== "ALL" && j.jobType !== type) return false;
      if (exp !== "ALL" && j.experienceLevel !== exp) return false;
      if (loc !== "ALL" && j.location !== loc) return false;
      if (!qq) return true;
      return [j.title, j.companyName, j.location].filter(Boolean).some((s) => s.toLowerCase().includes(qq));
    });
  }, [jobs, q, type, exp, loc]);

  const gridClass = settings.density === "compact" ? "flex flex-col gap-2.5" : "grid grid-cols-1 xl:grid-cols-2 gap-4";
  const variant = settings.density === "compact" ? "compact" : settings.density === "minimal" ? "minimal" : "detailed";

  return (
    <>
      <div className="flex items-end justify-between flex-wrap gap-4 mb-6">
        <div>
          <h1 className="text-[28px] font-extrabold tracking-tight">Browse all jobs</h1>
          <p className="text-muted text-[13.5px] font-medium mt-1">{jobs ? `${jobs.length} positions across the platform` : "Loading…"}</p>
        </div>
      </div>

      <div className="relative max-w-none mb-4.5">
        <Icon name="search" size={19} className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
        <input className="search-input" placeholder="Search by title, company, or location…" value={q} onChange={(e) => setQ(e.target.value)} />
      </div>

      <div className="flex flex-wrap gap-2.5 mb-5">
        <FilterPill label="Location" icon="location_on" value={loc} onChange={setLoc}
                    options={[{ value: "ALL", label: "Any location" }, ...locations.map((l) => ({ value: l, label: l }))]} />
        <FilterPill label="Job type" icon="work" value={type} onChange={setType}
                    options={[{ value: "ALL", label: "Any type" }, { value: "REMOTE", label: "Remote" }, { value: "HYBRID", label: "Hybrid" }, { value: "ONSITE", label: "Onsite" }, { value: "INTERNSHIP", label: "Internship" }]} />
        <FilterPill label="Experience" icon="stairs" value={exp} onChange={setExp}
                    options={[{ value: "ALL", label: "Any level" }, { value: "FRESHER", label: "Entry" }, { value: "JUNIOR", label: "Junior" }, { value: "MID", label: "Mid-level" }, { value: "SENIOR", label: "Senior" }]} />
      </div>

      {err && <Empty icon="error" title="Couldn't load jobs" subtitle={err} />}
      {!err && filtered === null && <div className={gridClass}>{[0, 1, 2, 3].map((i) => <JobCardSkeleton key={i} />)}</div>}
      {filtered && filtered.length === 0 && <Empty icon="search_off" title="No matching jobs" subtitle="Try clearing filters or a different keyword." />}
      {filtered && filtered.length > 0 && (
        <div className={gridClass}>
          {filtered.map((j) => (
            <JobCard key={j.id} job={j} variant={variant} saved={savedIds.has(j.id)} applied={appliedIds.has(j.id)}
                      onOpen={() => { setOpen(j); onInteract(j.id, "CLICKED"); }}
                      onSave={() => onInteract(j.id, "SAVED")} onApply={() => onInteract(j.id, "APPLIED")} />
          ))}
        </div>
      )}

      {open && (
        <JobDetailDrawer job={open} onClose={() => setOpen(null)} saved={savedIds.has(open.id)} applied={appliedIds.has(open.id)}
                          onSave={() => onInteract(open.id, "SAVED")} onApply={() => onInteract(open.id, "APPLIED")} />
      )}
    </>
  );
}
