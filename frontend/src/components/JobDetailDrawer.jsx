import { useState, useEffect } from "react";
import { Api } from "../lib/api.js";
import Icon from "./Icon.jsx";
import MatchRing from "./MatchRing.jsx";
import { JobTypePill, ExperiencePill } from "./Pills.jsx";

function DetailRow({ label, value, mono }) {
  return (
    <div className="bg-cream-2 border-2 border-ink rounded-lg px-3 py-2.5">
      <div className="text-[10px] text-faint uppercase tracking-wide font-bold">{label}</div>
      <div className={`text-[13px] mt-0.5 ${mono ? "font-mono" : ""}`}>{value || "—"}</div>
    </div>
  );
}

export default function JobDetailDrawer({ job, onClose, saved, applied, onSave, onApply }) {
  const [skills, setSkills] = useState(null);

  useEffect(() => {
    if (!job) return;
    let alive = true;
    setSkills(null);
    Api.jobSkills(job.id).then((s) => alive && setSkills(s)).catch(() => alive && setSkills([]));
    return () => { alive = false; };
  }, [job?.id]);

  useEffect(() => {
    const fn = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", fn);
    return () => window.removeEventListener("keydown", fn);
  }, [onClose]);

  if (!job) return null;
  const logoChar = (job.companyName || "?")[0]?.toUpperCase() || "?";

  return (
    <>
      <div className="fixed inset-0 bg-ink/30 z-50" onClick={onClose} />
      <div className="fixed top-0 right-0 bottom-0 w-full sm:w-[min(720px,92vw)] bg-cream border-l-2 border-ink z-[51] overflow-y-auto">
        <div className="sticky top-0 bg-cream border-b-2 border-ink px-7 py-4.5 flex items-center justify-between gap-3 z-10">
          <div className="flex items-center gap-3.5 min-w-0">
            <div className="job-logo !w-9.5 !h-9.5">{logoChar}</div>
            <div className="min-w-0">
              <div className="font-bold text-[15px]">{job.title}</div>
              <div className="text-xs text-muted">{job.companyName} · {job.location}</div>
            </div>
          </div>
          <button className="btn btn-icon btn-ghost" onClick={onClose} aria-label="Close"><Icon name="close" /></button>
        </div>

        <div className="p-7">
          <div className="flex gap-6 items-center mb-7 flex-wrap">
            <MatchRing value={(job.matchScore || 0) * 100} size={72} />
            <div className="flex gap-2 flex-wrap">
              <JobTypePill type={job.jobType} />
              <ExperiencePill level={job.experienceLevel} />
              {job.source && <span className="pill"><Icon name="link" size={13} />{job.source}</span>}
            </div>
            <div className="ml-auto flex gap-2">
              <button className="btn" onClick={onSave}>
                <Icon name="bookmark" filled={saved} />{saved ? "Saved" : "Save"}
              </button>
              <button
                className={`btn ${applied ? "" : "btn-primary"}`}
                onClick={() => { if (job.applyUrl) window.open(job.applyUrl, "_blank", "noopener"); onApply(); }}
                disabled={applied}
              >
                {applied ? <><Icon name="check" />Applied</> : <>Apply now<Icon name="open_in_new" /></>}
              </button>
            </div>
          </div>

          <div className="mb-8">
            <h3 className="font-extrabold text-[16px] flex items-center gap-2 mb-3"><Icon name="tag" className="text-[var(--accent-hover)]" />Required skills</h3>
            <div className="flex flex-wrap gap-1.5">
              {skills === null ? (
                <>
                  <div className="skeleton-block h-[26px] w-[70px] rounded-full" />
                  <div className="skeleton-block h-[26px] w-[90px] rounded-full" />
                  <div className="skeleton-block h-[26px] w-[60px] rounded-full" />
                </>
              ) : skills.length === 0 ? (
                <span className="text-[13px] text-muted">No skills specified for this job.</span>
              ) : (
                skills.map((s) => <span key={s.id} className="skill-chip">{s.name}</span>)
              )}
            </div>
          </div>

          <div className="mb-8">
            <h3 className="font-extrabold text-[16px] flex items-center gap-2 mb-3"><Icon name="description" className="text-[var(--accent-hover)]" />Description</h3>
            <p className="text-muted text-[13.5px] leading-7 whitespace-pre-wrap">
              {job.description || `${job.companyName} is hiring a ${job.title} based ${job.jobType?.toLowerCase()} in ${job.location}. Full description will appear here once the backend returns it.`}
            </p>
          </div>

          <div>
            <h3 className="font-extrabold text-[16px] flex items-center gap-2 mb-3"><Icon name="info" className="text-[var(--accent-hover)]" />Job details</h3>
            <div className="grid grid-cols-2 gap-3">
              <DetailRow label="Title" value={job.title} />
              <DetailRow label="Company" value={job.companyName} />
              <DetailRow label="Location" value={job.location} />
              <DetailRow label="Job type" value={job.jobType} />
              <DetailRow label="Experience" value={job.experienceLevel} />
              <DetailRow label="Source" value={job.source} mono />
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
