import { useState, useRef, useEffect } from "react";
import { Api } from "../lib/api.js";
import Icon from "../components/Icon.jsx";
import JobCard from "../components/JobCard.jsx";

export default function Chat({ user }) {
  const [messages, setMessages] = useState([
    { role: "ai", text: "Hi! Ask me anything about jobs on the platform — e.g. \"What remote React roles pay well?\" or \"Find me entry-level jobs in Berlin.\"", sources: [] },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const logRef = useRef(null);

  useEffect(() => { logRef.current?.scrollTo({ top: logRef.current.scrollHeight, behavior: "smooth" }); }, [messages, loading]);

  const send = async (e) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || loading) return;
    setMessages((m) => [...m, { role: "user", text }]);
    setInput("");
    setLoading(true);
    try {
      const res = await Api.chat(user.id, text);
      setMessages((m) => [...m, { role: "ai", text: res.answer, sources: res.sourceJobs || [] }]);
    } catch (err) {
      setMessages((m) => [...m, { role: "ai", text: `Couldn't reach the assistant — ${err.message}`, sources: [] }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="mb-6">
        <h1 className="text-[28px] font-extrabold tracking-tight">Ask AI</h1>
        <p className="text-muted text-[13.5px] font-medium mt-1">Chat with an assistant that knows the job listings.</p>
      </div>

      <div className="card flex flex-col h-[calc(100vh-260px)] max-h-[720px]">
        <div className="flex-1 overflow-y-auto flex flex-col gap-3.5 pb-4" ref={logRef}>
          {messages.map((m, i) => (
            <div key={i} className={`chat-bubble ${m.role === "user" ? "chat-bubble-user" : "chat-bubble-ai"}`}>
              {m.text}
              {m.sources?.length > 0 && (
                <div className="flex flex-col gap-2 mt-2.5">
                  {m.sources.map((j) => <JobCard key={j.id} job={j} variant="compact" onOpen={() => (window.location.hash = "#/jobs")} />)}
                </div>
              )}
            </div>
          ))}
          {loading && (
            <div className="chat-bubble chat-bubble-ai flex items-center gap-2">
              <span className="w-4 h-4 border-[2.5px] border-cream-3 border-t-ink rounded-full animate-spin inline-block" /> thinking…
            </div>
          )}
        </div>
        <form className="flex gap-2.5 mt-3.5" onSubmit={send}>
          <input className="field-input flex-1" placeholder="Ask about jobs, skills, or companies…" value={input} onChange={(e) => setInput(e.target.value)} disabled={loading} />
          <button type="submit" className="btn btn-primary" disabled={loading || !input.trim()}><Icon name="send" /></button>
        </form>
      </div>
    </>
  );
}
