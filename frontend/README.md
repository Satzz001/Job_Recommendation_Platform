# jobrecomm frontend

React + Vite + Tailwind CSS frontend for the jobplatform Spring Boot backend.

## Setup

```bash
npm install
cp .env.example .env   # points at your backend, defaults to http://localhost:8080
npm run dev
```

Open the printed localhost URL. Start your Spring Boot backend separately on port 8080.

## Editing the design

All visual styling is plain Tailwind classes plus a small set of reusable component
classes defined with `@apply` in `src/index.css` (`.card`, `.btn`, `.pill`, etc.) —
edit those directly, no build tooling knowledge beyond Tailwind needed.

Theme tokens (colors, radius, shadows) live in `tailwind.config.js`. The accent color
is a CSS variable (`--accent` etc., set in `src/index.css` and swapped at runtime by
`SettingsContext`) so the in-app "Appearance" panel can change it live — override the
defaults in `:root` there if you want a different starting color.

## Backend requirements

- CORS must be enabled for all controllers (not just `/api/resumes/**`).
- JWT payload should include `id` (or `userId`) — the frontend decodes it client-side
  to know which user is logged in.
- Endpoints used: see `src/lib/api.js`.
