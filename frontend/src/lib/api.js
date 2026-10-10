const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

export class ApiError extends Error {
  constructor(msg, status, data) {
    super(msg);
    this.status = status;
    this.data = data;
  }
}

export const Auth = {
  token: () => localStorage.getItem("jr_token") || null,
  user: () => {
    try { return JSON.parse(localStorage.getItem("jr_user") || "null"); }
    catch { return null; }
  },
  setSession: (token, user) => {
    if (token) localStorage.setItem("jr_token", token);
    if (user) localStorage.setItem("jr_user", JSON.stringify(user));
  },
  clear: () => {
    localStorage.removeItem("jr_token");
    localStorage.removeItem("jr_user");
  },
};

export function decodeJwt(token) {
  try {
    const [, payload] = token.split(".");
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json);
  } catch { return null; }
}

async function request(path, opts = {}) {
  const headers = { ...(opts.headers || {}) };
  if (!opts.isForm && opts.body) headers["Content-Type"] = "application/json";
  const tok = Auth.token();
  if (tok) headers["Authorization"] = "Bearer " + tok;

  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method: opts.method || "GET",
      headers,
      body: opts.isForm ? opts.body : (opts.body ? JSON.stringify(opts.body) : undefined),
    });
  } catch {
    throw new ApiError(`Network error — is the backend running on ${API_BASE}?`, 0);
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }

  if (!res.ok) {
    const msg = typeof data === "string" ? data : (data?.error || data?.message || `Request failed (${res.status})`);
    throw new ApiError(msg, res.status, data);
  }
  return data;
}

export const Api = {
  login: (email, password) => request("/api/auth/login", { method: "POST", body: { email, password } }),
  register: (name, email, password) => request("/api/auth/register", { method: "POST", body: { name, email, password } }),

  allJobs: () => request("/api/jobs"),
  jobSkills: (jobId) => request(`/api/jobs/${jobId}/skills`),

  recommendations: (userId, page = 0, size = 20) =>
    request(`/api/users/${userId}/recommendations?page=${page}&size=${size}`),
  userSkills: (userId) => request(`/api/users/${userId}/skills`),
  addUserSkill: (userId, skillId) => request(`/api/users/${userId}/skills/${skillId}`, { method: "POST" }),
  interact: (userId, jobId, type) =>
    request(`/api/users/${userId}/jobs/${jobId}/interaction?type=${type}`, { method: "POST" }),

  allSkills: () => request("/api/skills"),
  createSkill: (name) => request("/api/skills", { method: "POST", body: { name } }),

  parseResume: (userId, file) => {
    const fd = new FormData();
    fd.append("userId", userId);
    fd.append("file", file);
    return request("/api/resumes/parse", { method: "POST", body: fd, isForm: true });
  },
  resumeHealth: () => request("/api/resumes/health"),

  chat: (userId, message) => request(`/api/users/${userId}/chat`, { method: "POST", body: { message } }),
};
