export default function Avatar({ name, size = "md" }) {
  const initials = (name || "?").split(/\s+/).filter(Boolean).slice(0, 2).map((s) => s[0]?.toUpperCase()).join("");
  const sizes = { md: "w-[34px] h-[34px] text-xs", lg: "w-12 h-12 text-base", xl: "w-[76px] h-[76px] text-2xl border-[3px]" };
  return (
    <div className={`rounded-full bg-cream-2 border-2 border-ink grid place-items-center font-extrabold shrink-0 ${sizes[size]}`}>
      {initials || "?"}
    </div>
  );
}
