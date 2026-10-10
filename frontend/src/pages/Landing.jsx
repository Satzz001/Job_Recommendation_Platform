import { Link } from "react-router-dom";

const HOW_IT_WORKS = [
  {
    title: "Create your profile",
    copy: "Share your skills, role preferences, and goals to personalize your feed from day one.",
  },
  {
    title: "Discover relevant jobs",
    copy: "See opportunities ranked by fit so you can focus on the roles that match your strengths.",
  },
  {
    title: "Take the next step",
    copy: "Track progress, stay organized, and apply with confidence when the timing is right.",
  },
];

const FEATURES = [
  {
    title: "Personalized recommendations",
    copy: "Recommendations adapt to your profile and interactions so your matches keep getting better.",
  },
  {
    title: "Smarter discovery",
    copy: "Find high-signal roles faster with focused insights instead of endless generic listings.",
  },
  {
    title: "Career-focused matching",
    copy: "Identify opportunities aligned with your long-term growth, not just your last job title.",
  },
];

function HeaderNavLink({ href, children }) {
  return (
    <a
      href={href}
      className="text-[13.5px] font-semibold text-muted hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink focus-visible:ring-offset-2 focus-visible:ring-offset-cream rounded-md px-1.5 py-1"
    >
      {children}
    </a>
  );
}

export default function Landing() {
  return (
    <div className="min-h-screen bg-cream text-ink">
      <header className="border-b-2 border-ink bg-cream">
        <div className="max-w-[1160px] mx-auto px-4 sm:px-6 py-4 flex items-center justify-between gap-3">
          <a
            href="#top"
            className="flex items-center gap-2.5 font-extrabold text-base focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink focus-visible:ring-offset-2 focus-visible:ring-offset-cream rounded-lg"
          >
            <span
              className="w-[30px] h-[30px] rounded-lg border-2 border-ink grid place-items-center font-extrabold text-sm shadow-brutal-sm"
              style={{ background: "var(--accent)", color: "var(--accent-contrast)" }}
              aria-hidden="true"
            >
              jr
            </span>
            <span>Jobs4AI</span>
          </a>

          <nav aria-label="Landing page sections" className="hidden md:flex items-center gap-2.5">
            <HeaderNavLink href="#how-it-works">How it works</HeaderNavLink>
            <HeaderNavLink href="#features">Features</HeaderNavLink>
            <HeaderNavLink href="#for-job-seekers">For job seekers</HeaderNavLink>
          </nav>

          <div className="flex items-center gap-2.5">
            <Link to="/login" className="btn btn-ghost">
              Sign in
            </Link>
            <Link to="/register" className="btn btn-primary">
              Get started
            </Link>
          </div>
        </div>
      </header>

      <main id="top">
        <section className="max-w-[1160px] mx-auto px-4 sm:px-6 py-14 sm:py-18 grid lg:grid-cols-[1.05fr_0.95fr] gap-10 items-center">
          <div>
            <p className="pill mb-4">jobs4ai.me</p>
            <h1 className="text-[33px] sm:text-[46px] leading-[1.1] font-extrabold tracking-tight max-w-[16ch]">
              Find the right opportunity. Build the right future.
            </h1>
            <p className="mt-5 text-muted text-[15px] sm:text-[16px] font-medium max-w-[56ch] leading-relaxed">
              Jobs4AI helps job seekers discover personalized opportunities based on skills, experience, and career direction—so each application is more focused and meaningful.
            </p>
            <div className="mt-7 flex flex-wrap gap-3">
              <Link to="/register" className="btn btn-primary btn-lg">
                Get started
              </Link>
              <a href="#how-it-works" className="btn btn-lg">
                See how it works
              </a>
            </div>
          </div>

          <div className="card p-4 sm:p-5 bg-white relative overflow-hidden">
            <div
              className="absolute -top-8 -right-8 w-24 h-24 rounded-full border-2 border-ink bg-cream-2"
              aria-hidden="true"
            />
            <div className="relative">
              <div className="flex items-center justify-between gap-3 mb-4">
                <div>
                  <p className="text-[11px] uppercase tracking-wide text-faint font-bold">Recommendation preview</p>
                  <h2 className="text-[21px] font-extrabold mt-0.5">Your best-fit roles</h2>
                </div>
                <span className="pill pill-accent">AI matched</span>
              </div>
              <div className="space-y-3">
                {[
                  { company: "Nova Labs", role: "Frontend Engineer", score: "95%" },
                  { company: "Pixelgrid", role: "Product Engineer", score: "89%" },
                  { company: "ScalePath", role: "Platform Developer", score: "83%" },
                ].map((job) => (
                  <div key={job.company} className="rounded-xl border-2 border-ink bg-cream-2 px-3.5 py-3 shadow-brutal-sm">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-[13px] font-bold">{job.role}</p>
                        <p className="text-[12px] text-muted">{job.company} · Remote / Hybrid</p>
                      </div>
                      <span className="rounded-full border-2 border-ink bg-white px-2.5 py-1 text-[11px] font-extrabold">{job.score}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="border-y-2 border-ink bg-cream-2">
          <div className="max-w-[1160px] mx-auto px-4 sm:px-6 py-5 grid sm:grid-cols-3 gap-4 text-center sm:text-left">
            <div>
              <p className="text-[22px] font-extrabold">3 steps</p>
              <p className="text-[13px] text-muted font-medium">From profile to qualified applications.</p>
            </div>
            <div>
              <p className="text-[22px] font-extrabold">Focused matches</p>
              <p className="text-[13px] text-muted font-medium">Recommendations aligned with your direction.</p>
            </div>
            <div>
              <p className="text-[22px] font-extrabold">jobs4ai.me</p>
              <p className="text-[13px] text-muted font-medium">Built for modern, intentional job search.</p>
            </div>
          </div>
        </section>

        <section id="how-it-works" className="max-w-[1160px] mx-auto px-4 sm:px-6 py-14">
          <h2 className="text-[28px] font-extrabold tracking-tight">How it works</h2>
          <p className="text-muted font-medium mt-2 max-w-[62ch]">A clear and practical workflow to help you spend less time searching and more time moving forward.</p>
          <div className="mt-7 grid md:grid-cols-3 gap-4">
            {HOW_IT_WORKS.map((item, index) => (
              <article key={item.title} className="card">
                <span className="pill mb-3">Step {index + 1}</span>
                <h3 className="text-[18px] font-extrabold">{item.title}</h3>
                <p className="text-muted text-[14px] font-medium mt-2 leading-relaxed">{item.copy}</p>
              </article>
            ))}
          </div>
        </section>

        <section id="features" className="max-w-[1160px] mx-auto px-4 sm:px-6 pb-14">
          <h2 className="text-[28px] font-extrabold tracking-tight">Features that keep your search on track</h2>
          <div className="mt-7 grid md:grid-cols-3 gap-4">
            {FEATURES.map((item) => (
              <article key={item.title} className="card bg-white">
                <h3 className="text-[18px] font-extrabold">{item.title}</h3>
                <p className="text-muted text-[14px] font-medium mt-2 leading-relaxed">{item.copy}</p>
              </article>
            ))}
          </div>
        </section>

        <section id="for-job-seekers" className="px-4 sm:px-6 pb-14">
          <div className="max-w-[1160px] mx-auto card bg-cream-2 flex flex-col md:flex-row md:items-center md:justify-between gap-5">
            <div>
              <h2 className="text-[27px] font-extrabold tracking-tight">For job seekers</h2>
              <p className="text-muted text-[14px] font-medium mt-2 max-w-[62ch]">
                Make your job search more focused, efficient, and outcome-driven with recommendations built around where you want to grow next.
              </p>
            </div>
            <Link to="/register" className="btn btn-primary btn-lg self-start md:self-auto">
              Create your profile
            </Link>
          </div>
        </section>
      </main>

      <footer className="border-t-2 border-ink bg-white">
        <div className="max-w-[1160px] mx-auto px-4 sm:px-6 py-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2.5">
          <p className="text-[12.5px] text-muted font-medium">© 2026 Jobs4AI · jobs4ai.me</p>
          <nav aria-label="Footer links" className="flex flex-wrap items-center gap-x-4 gap-y-1 text-[12.5px] font-semibold">
            <a href="#top" className="hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink focus-visible:ring-offset-2 focus-visible:ring-offset-white rounded-sm px-1">Home</a>
            <a href="#" className="hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink focus-visible:ring-offset-2 focus-visible:ring-offset-white rounded-sm px-1">Privacy</a>
            <a href="#" className="hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink focus-visible:ring-offset-2 focus-visible:ring-offset-white rounded-sm px-1">Terms</a>
            <Link to="/login" className="hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink focus-visible:ring-offset-2 focus-visible:ring-offset-white rounded-sm px-1">Sign in</Link>
          </nav>
        </div>
      </footer>
    </div>
  );
}
