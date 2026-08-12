import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Api, Auth, decodeJwt } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import MatchRing from "../components/MatchRing.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { useToast } from "../context/ToastContext.jsx";

export function AuthArtSide() {
  const rows = [
    { logo: "S", title: "Senior Backend Engineer", meta: "Stripe · Remote", score: 94 },
    { logo: "V", title: "Full-Stack Developer", meta: "Vercel · Hybrid", score: 87 },
    { logo: "L", title: "Platform Engineer", meta: "Linear · Remote", score: 81 },
  ];
  return (
    <div className="hidden lg:block relative overflow-hidden border-l-2 border-ink" style={{ background: "var(--accent-dim)" }}>
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[360px] flex flex-col gap-4">
        {rows.map((r) => (
          <div key={r.title} className="card flex items-center gap-3">
            <div className="job-logo">{r.logo}</div>
            <div className="flex-1">
              <div className="text-[13px] font-bold">{r.title}</div>
              <div className="text-xs text-muted">{r.meta}</div>
            </div>
            <MatchRing value={r.score} size={42} />
          </div>
        ))}
      </div>
    </div>
  );
}

export function BrandLockup() {
  return (
    <div className="absolute top-9 left-[8vw] flex items-center gap-2.5 font-extrabold text-base">
      <div className="w-[30px] h-[30px] rounded-lg border-2 border-ink grid place-items-center font-extrabold text-sm shadow-brutal-sm" style={{ background: "var(--accent)", color: "var(--accent-contrast)" }}>jr</div>
      <span>jobrecomm</span>
    </div>
  );
}

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [pw, setPw] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setErr(null);
    setLoading(true);
    try {
      const token = await Api.login(email, pw);
      const payload = decodeJwt(token) || {};
      const user = { id: payload.id || payload.userId || payload.sub || 1, email: payload.email || payload.sub || email, name: payload.name || email.split("@")[0] };
      Auth.setSession(token, user);
      login(user);
      toast.good(`Welcome back, ${user.name}`);
      navigate("/dashboard");
    } catch (e) {
      setErr(e.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-cream">
      <div className="flex flex-col justify-center px-8 lg:px-[8vw] relative py-20">
        <BrandLockup />
        <form className="w-full max-w-[380px] mx-auto" onSubmit={submit}>
          <h1 className="text-[30px] font-extrabold tracking-tight mb-1.5">Welcome back</h1>
          <p className="text-muted text-[13.5px] mb-8 font-medium">Sign in to see jobs matched to your skills.</p>

          <div className="mb-4 flex flex-col gap-1.5">
            <label className="field-label" htmlFor="email">EMAIL</label>
            <input id="email" type="email" className="field-input" autoComplete="email" required placeholder="you@domain.com" value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="mb-4 flex flex-col gap-1.5 relative">
            <label className="field-label" htmlFor="pw">PASSWORD</label>
            <input id="pw" type={showPw ? "text" : "password"} className="field-input" autoComplete="current-password" required placeholder="••••••••" value={pw} onChange={(e) => setPw(e.target.value)} />
            <button type="button" className="absolute right-2.5 top-[30px] text-faint hover:text-ink" onClick={() => setShowPw((s) => !s)} tabIndex={-1}>
              <Icon name={showPw ? "visibility_off" : "visibility"} size={18} />
            </button>
          </div>

          {err && <div className="text-xs text-red-600 font-semibold mb-3 flex items-center gap-1"><Icon name="error" size={13} />{err}</div>}

          <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={loading}>
            {loading ? <span className="w-4 h-4 border-[2.5px] border-cream-3 border-t-ink rounded-full animate-spin inline-block" /> : <Icon name="login" />}
            Sign in
          </button>

          <div className="flex items-center gap-3 my-5 text-faint text-[11px] font-bold uppercase tracking-wide">
            <div className="flex-1 h-0.5 bg-ink" /> or <div className="flex-1 h-0.5 bg-ink" />
          </div>
          <button type="button" className="btn btn-block btn-lg" disabled>
            <Icon name="passkey" /> Continue with SSO
          </button>

          <div className="mt-6 text-[13px] text-muted text-center font-medium">
            New here? <Link to="/register" className="font-bold">Create an account</Link>
          </div>
        </form>
      </div>
      <AuthArtSide />
    </div>
  );
}

export function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [pw, setPw] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const issues = [];
  if (pw.length > 0) {
    if (pw.length < 8) issues.push("8+ characters");
    if (!/[a-z]/.test(pw)) issues.push("a lowercase letter");
    if (!/[A-Z]/.test(pw)) issues.push("an uppercase letter");
    if (!/[0-9]/.test(pw)) issues.push("a number");
  }

  const submit = async (e) => {
    e.preventDefault();
    setErr(null);
    if (issues.length > 0) { setErr("Password must contain " + issues.join(", ")); return; }
    setLoading(true);
    try {
      await Api.register(name, email, pw);
      toast.good("Account created — signing you in…");
      const token = await Api.login(email, pw);
      const payload = decodeJwt(token) || {};
      const user = { id: payload.id || payload.userId || payload.sub || 1, email: payload.email || email, name: payload.name || name };
      Auth.setSession(token, user);
      login(user);
      navigate("/dashboard");
    } catch (e) {
      setErr(e.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-cream">
      <div className="flex flex-col justify-center px-8 lg:px-[8vw] relative py-20">
        <BrandLockup />
        <form className="w-full max-w-[380px] mx-auto" onSubmit={submit}>
          <h1 className="text-[30px] font-extrabold tracking-tight mb-1.5">Create your account</h1>
          <p className="text-muted text-[13.5px] mb-8 font-medium">Upload a resume, see jobs that actually fit.</p>

          <div className="mb-4 flex flex-col gap-1.5">
            <label className="field-label" htmlFor="rname">FULL NAME</label>
            <input id="rname" className="field-input" required placeholder="Jane Doe" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="mb-4 flex flex-col gap-1.5">
            <label className="field-label" htmlFor="remail">EMAIL</label>
            <input id="remail" type="email" className="field-input" required placeholder="you@domain.com" value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="mb-4 flex flex-col gap-1.5 relative">
            <label className="field-label" htmlFor="rpw">PASSWORD</label>
            <input id="rpw" type={showPw ? "text" : "password"} className="field-input" required placeholder="At least 8 characters" value={pw} onChange={(e) => setPw(e.target.value)} />
            <button type="button" className="absolute right-2.5 top-[30px] text-faint hover:text-ink" onClick={() => setShowPw((s) => !s)} tabIndex={-1}>
              <Icon name={showPw ? "visibility_off" : "visibility"} size={18} />
            </button>
            {pw.length > 0 && issues.length > 0 && <div className="text-[11px] text-faint mt-1">Needs: {issues.join(", ")}</div>}
            {pw.length > 0 && issues.length === 0 && <div className="text-[11px] text-green-700 mt-1 flex items-center gap-1"><Icon name="check_circle" size={12} />Strong password</div>}
          </div>

          {err && <div className="text-xs text-red-600 font-semibold mb-3 flex items-center gap-1"><Icon name="error" size={13} />{err}</div>}

          <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={loading}>
            {loading ? <span className="w-4 h-4 border-[2.5px] border-cream-3 border-t-ink rounded-full animate-spin inline-block" /> : <Icon name="person_add" />}
            Create account
          </button>

          <div className="mt-6 text-[13px] text-muted text-center font-medium">
            Have an account? <Link to="/login" className="font-bold">Sign in</Link>
          </div>
        </form>
      </div>
      <AuthArtSide />
    </div>
  );
}
