import { useState, useRef, useCallback } from "react";
import { Api } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import { useToast } from "../context/ToastContext.jsx";
import { Link } from "react-router-dom";

function FeatureBullet({ icon, title, sub }) {
  return (
    <div className="flex gap-3 items-start">
      <div className="w-8 h-8 rounded-lg bg-cream-2 border-2 border-ink grid place-items-center shrink-0">
        <Icon name={icon} size={18} className="text-[var(--accent-hover)]" />
      </div>
      <div>
        <div className="text-[13px] font-bold">{title}</div>
        <div className="text-xs text-muted font-medium">{sub}</div>
      </div>
    </div>
  );
}

function PipelineStep({ label, done, active }) {
  return (
    <div className={`flex items-center gap-2.5 text-[13px] font-semibold ${done || active ? "text-ink" : "text-faint"}`}>
      {done ? <Icon name="check_circle" filled size={18} className="text-green-700" /> :
        active ? <span className="w-4 h-4 border-2 border-cream-3 border-t-ink rounded-full animate-spin inline-block" /> :
        <Icon name="radio_button_unchecked" size={18} />}
      {label}
    </div>
  );
}

function StatBox({ label, value, accent, warn }) {
  return (
    <div className={`rounded-xl border-2 border-ink px-4 py-3.5 ${accent ? "" : warn ? "" : "bg-cream-2"}`} style={accent ? { background: "var(--accent-dim)" } : warn ? { background: "#FEF3C7" } : undefined}>
      <div className="text-[10px] text-faint uppercase tracking-wide font-bold">{label}</div>
      <div className={`text-xl font-extrabold mt-1 ${warn ? "text-amber-700" : ""}`}>{value}</div>
    </div>
  );
}

export default function Resume({ user }) {
  const [drag, setDrag] = useState(false);
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState(null);
  const [err, setErr] = useState(null);
  const fileRef = useRef(null);
  const toast = useToast();

  const handleFile = useCallback(async (f) => {
    if (!f) return;
    if (f.type !== "application/pdf" && !f.name.toLowerCase().endsWith(".pdf")) { toast.err("Please upload a PDF file"); return; }
    setFile(f);
    setStatus("uploading");
    setErr(null);
    setProgress(10);
    const tick = setInterval(() => setProgress((p) => Math.min(p + Math.random() * 12, 88)), 350);
    try {
      setTimeout(() => setStatus("parsing"), 600);
      const res = await Api.parseResume(user.id, f);
      clearInterval(tick);
      setProgress(100);
      setStatus("done");
      setResult(res);
      toast.good(`Parsed — ${res.skillsCount || 0} skills extracted`);
    } catch (e) {
      clearInterval(tick);
      setStatus("error");
      setErr(e.message || "Failed to parse resume");
      toast.err(e.message || "Failed to parse resume");
    }
  }, [user.id, toast]);

  const reset = () => { setFile(null); setStatus("idle"); setProgress(0); setResult(null); setErr(null); };

  return (
    <>
      <div className="mb-6">
        <h1 className="text-[28px] font-extrabold tracking-tight">Upload your resume</h1>
        <p className="text-muted text-[13.5px] font-medium mt-1">We'll extract your skills and use them to find matching jobs.</p>
      </div>

      {status === "idle" && (
        <div className="card !p-8">
          <div className={`border-[3px] border-dashed border-ink rounded-xl2 py-12 px-6 text-center cursor-pointer transition-colors ${drag ? "bg-white shadow-brutal" : ""}`}
               style={!drag ? { background: "var(--accent-dim)" } : undefined}
               onClick={() => fileRef.current?.click()}
               onDragOver={(e) => { e.preventDefault(); setDrag(true); }}
               onDragLeave={() => setDrag(false)}
               onDrop={(e) => { e.preventDefault(); setDrag(false); handleFile(e.dataTransfer.files?.[0]); }}>
            <div className="w-14 h-14 rounded-2xl bg-white border-2 border-ink shadow-brutal-sm grid place-items-center mx-auto mb-4 text-[var(--accent-hover)]">
              <Icon name="upload_file" size={28} />
            </div>
            <div className="text-[15.5px] font-extrabold mb-1.5">Drop your PDF resume here</div>
            <div className="text-muted text-[13px] font-medium">or click to browse · max 10 MB · PDF only</div>
            <input ref={fileRef} type="file" accept="application/pdf,.pdf" className="hidden" onChange={(e) => handleFile(e.target.files?.[0])} />
          </div>
          <div className="mt-7 grid sm:grid-cols-3 gap-3">
            <FeatureBullet icon="bolt" title="Fast AI parsing" sub="Skills, experience and roles in under 10 seconds" />
            <FeatureBullet icon="enhanced_encryption" title="Private" sub="Your resume is never shared with employers" />
            <FeatureBullet icon="auto_awesome" title="Sharper matches" sub="Recommendations improve immediately" />
          </div>
        </div>
      )}

      {(status === "uploading" || status === "parsing") && (
        <div className="card !p-8">
          <div className="flex items-center gap-4 mb-5.5">
            <div className="w-14 h-14 rounded-2xl bg-cream-2 border-2 border-ink grid place-items-center">
              <Icon name="picture_as_pdf" size={26} className="text-[var(--accent-hover)]" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="font-bold">{file?.name}</div>
              <div className="text-xs text-muted font-medium">{(file?.size / 1024).toFixed(0)} KB · {status === "uploading" ? "Uploading…" : "Parsing with AI…"}</div>
            </div>
            <span className="w-4 h-4 border-[2.5px] border-cream-3 border-t-ink rounded-full animate-spin inline-block" />
          </div>
          <div className="h-1.5 bg-cream-2 border border-ink rounded-full overflow-hidden">
            <div className="h-full transition-all" style={{ width: `${progress}%`, background: "var(--accent)" }} />
          </div>
          <div className="mt-5.5 flex flex-col gap-2.5">
            <PipelineStep label="Extract text from PDF" done={progress > 30} active={progress <= 30} />
            <PipelineStep label="Parse skills & experience with AI" done={progress > 70} active={progress > 30 && progress <= 70} />
            <PipelineStep label="Save to your profile" done={progress >= 100} active={progress > 70 && progress < 100} />
          </div>
        </div>
      )}

      {status === "error" && (
        <div className="card !p-8">
          <div className="flex items-center gap-3.5 mb-3.5">
            <Icon name="error" size={28} className="text-red-600" />
            <div>
              <div className="font-bold">Couldn't parse resume</div>
              <div className="text-[13px] text-muted font-medium">{err}</div>
            </div>
          </div>
          <button className="btn" onClick={reset}><Icon name="restart_alt" />Try again</button>
        </div>
      )}

      {status === "done" && result && (
        <div className="card !p-8">
          <div className="flex items-center gap-3.5 mb-5.5">
            <div className="w-11 h-11 rounded-xl grid place-items-center border-2 border-ink" style={{ background: "var(--accent-dim)" }}>
              <Icon name="check" filled className="text-[var(--accent-hover)]" />
            </div>
            <div>
              <div className="font-extrabold text-base">Resume parsed</div>
              <div className="text-[13px] text-muted font-medium">{result.message}</div>
            </div>
            <button className="btn btn-ghost ml-auto" onClick={reset}><Icon name="add" />Upload another</button>
          </div>

          <div className="grid sm:grid-cols-3 gap-3 mb-6">
            <StatBox label="Skills extracted" value={result.skillsCount || 0} accent />
            <StatBox label="Profile fields updated" value={(result.fieldsUpdated || []).length} />
            <StatBox label="Fields missing" value={(result.fieldsMissing || []).length} warn={(result.fieldsMissing || []).length > 0} />
          </div>

          {(result.skillsAdded || []).length > 0 && (
            <div className="mb-6">
              <h3 className="font-extrabold text-base flex items-center gap-2 mb-3"><Icon name="psychology" className="text-[var(--accent-hover)]" />Extracted skills</h3>
              <div className="flex flex-wrap gap-1.5">
                {result.skillsAdded.map((s) => <span key={s} className="skill-chip skill-chip-matched"><Icon name="check" size={13} />{s}</span>)}
              </div>
            </div>
          )}

          {(result.fieldsMissing || []).length > 0 && (
            <div className="mb-6">
              <h3 className="font-extrabold text-base flex items-center gap-2 mb-3"><Icon name="report" className="text-amber-700" />Couldn't find</h3>
              <div className="flex flex-wrap gap-1.5">
                {result.fieldsMissing.map((s) => <span key={s} className="pill pill-warn">{s}</span>)}
              </div>
              <p className="text-xs text-muted font-medium mt-3">Add these manually from your <Link to="/profile" className="font-bold">profile</Link>.</p>
            </div>
          )}

          <div className="flex gap-2">
            <Link to="/dashboard" className="btn btn-primary"><Icon name="auto_awesome" />See new matches</Link>
            <Link to="/profile" className="btn">View profile</Link>
          </div>
        </div>
      )}
    </>
  );
}
