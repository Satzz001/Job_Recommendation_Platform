import { useState, useEffect } from "react";
import { Api } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import JobCard from "../components/JobCard.jsx";
import { JobCardSkeleton } from "../components/JobCardSkeleton.jsx";
import Empty from "../components/Empty.jsx";
import JobDetailDrawer from "../components/JobDetailDrawer.jsx";
import { Link } from "react-router-dom";
import { useSettings } from "../context/SettingsContext.jsx";

export default function Activity({ savedIds, appliedIds, onInteract }) {
  const { settings } = useSettings();
  const [tab, setTab] = useState("saved");
  const [jobs, setJobs] = useState(null);
  const [open, setOpen] = useState(null);

  useEffect(() => { Api.allJobs().then(setJobs).catch(() => setJobs([])); }, []);

  const visibleIds = tab === "saved" ? savedIds : appliedIds;
  const list = jobs ? jobs.filter((j) => visibleIds.has(j.id)) : null;
  const gridClass = settings.density === "compact" ? "flex flex-col gap-2.5" : "grid grid-cols-1 xl:grid-cols-2 gap-4";
  const variant = settings.density === "compact" ? "compact" : "detailed";

  return (
    <>
      <div className="mb-6">
        <h1 className="text-[28px] font-extrabold tracking-tight">Your activity</h1>
        <p className="text-muted text-[13.5px] font-medium mt-1">Jobs you've saved or applied to.</p>
      </div>

      <div className="flex gap-1 border-b-2 border-ink mb-6">
        <button className={`tabs-tab ${tab === "saved" ? "tabs-tab-active" : ""}`} onClick={() => setTab("saved")}>
          <Icon name="bookmark" filled={tab === "saved"} size={18} /> Saved <span className="bg-[var(--accent-dim)] font-extrabold text-[10.5px] px-1.5 py-0.5 rounded-full">{savedIds.size}</span>
        </button>
        <button className={`tabs-tab ${tab === "applied" ? "tabs-tab-active" : ""}`} onClick={() => setTab("applied")}>
          <Icon name="send" filled={tab === "applied"} size={18} /> Applied <span className="bg-[var(--accent-dim)] font-extrabold text-[10.5px] px-1.5 py-0.5 rounded-full">{appliedIds.size}</span>
        </button>
      </div>

      {list === null && <div className={gridClass}>{[0, 1, 2].map((i) => <JobCardSkeleton key={i} />)}</div>}
      {list && list.length === 0 && (
        <Empty icon={tab === "saved" ? "bookmark_border" : "send"}
               title={tab === "saved" ? "No saved jobs yet" : "No applications yet"}
               subtitle={tab === "saved" ? "Bookmark jobs to revisit later." : "Apply to jobs that match your skills."}
               action={<Link to="/jobs" className="btn btn-primary"><Icon name="search" />Browse jobs</Link>} />
      )}
      {list && list.length > 0 && (
        <div className={gridClass}>
          {list.map((j) => (
            <JobCard key={j.id} job={j} variant={variant} saved={savedIds.has(j.id)} applied={appliedIds.has(j.id)}
                      onOpen={() => setOpen(j)} onSave={() => onInteract(j.id, "SAVED")} onApply={() => onInteract(j.id, "APPLIED")} />
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
