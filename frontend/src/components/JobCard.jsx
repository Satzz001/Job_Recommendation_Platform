import Icon from "./Icon.jsx";
import MatchRing from "./MatchRing.jsx";
import { JobTypePill, ExperiencePill } from "./Pills.jsx";

export default function JobCard({ job, variant = "detailed", saved, applied, onOpen, onSave, onApply }) {
  const logoChar = (job.companyName || "?").trim()[0]?.toUpperCase() || "?";

  const doApply = () => {
    if (job.applyUrl) window.open(job.applyUrl, "_blank", "noopener");
    onApply?.();
  };

  if (variant === "compact") {
    return (
      <div className="grid grid-cols-[36px_1fr_auto_auto_auto] gap-3.5 items-center px-4.5 py-3 bg-white border-2 border-ink rounded-xl shadow-brutal-sm hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-brutal transition-transform cursor-pointer" onClick={onOpen}>
        <div className="job-logo !w-9 !h-9 !text-[13px]">{logoChar}</div>
        <div className="min-w-0">
          <h3 className="font-extrabold text-sm">{job.title}</h3>
          <div className="text-xs text-muted">{job.companyName} · {job.location}</div>
        </div>
        <JobTypePill type={job.jobType} />
        <MatchRing value={(job.matchScore || 0) * 100} size={38} />
        <button className="btn btn-icon btn-ghost" onClick={(e) => { e.stopPropagation(); onSave?.(); }} title="Save">
          <Icon name="bookmark" filled={saved} size={18} />
        </button>
      </div>
    );
  }

  const minimal = variant === "minimal";
  return (
    <div className={`card card-interactive job-card flex gap-4 items-start ${minimal ? "!p-4" : ""}`} onClick={onOpen}>
      <div className="job-logo">{logoChar}</div>
      <div className="flex-1 min-w-0">
        <div className="flex items-start gap-3 justify-between">
          <div className="min-w-0 flex-1">
            <h3 className="font-extrabold text-[16px] leading-tight mb-0.5">{job.title}</h3>
            <div className="text-[13px] text-muted"><b className="text-ink font-bold">{job.companyName}</b> · {job.location}</div>
          </div>
          {!minimal && <MatchRing value={(job.matchScore || 0) * 100} />}
        </div>
        {!minimal && (
          <div className="flex flex-wrap gap-1.5 mt-2.5">
            <JobTypePill type={job.jobType} />
            <ExperiencePill level={job.experienceLevel} />
            {job.source && <span className="pill"><Icon name="link" size={13} />{job.source}</span>}
          </div>
        )}
        <div className="flex gap-1.5 items-center mt-3">
          <button className={`btn btn-sm ${saved ? "btn-ghost" : ""}`} onClick={(e) => { e.stopPropagation(); onSave?.(); }}>
            <Icon name="bookmark" filled={saved} size={16} />
            {saved ? "Saved" : "Save"}
          </button>
          <button className={`btn btn-sm ${applied ? "" : "btn-primary"}`} onClick={(e) => { e.stopPropagation(); doApply(); }} disabled={applied}>
            {applied ? <><Icon name="check" size={16} />Applied</> : <>Apply<Icon name="arrow_forward" size={16} /></>}
          </button>
        </div>
      </div>
    </div>
  );
}
