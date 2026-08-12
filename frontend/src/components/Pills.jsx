import Icon from "./Icon.jsx";

const TYPE_MAP = {
  REMOTE: { icon: "public", label: "Remote" },
  ONSITE: { icon: "domain", label: "Onsite" },
  HYBRID: { icon: "join_inner", label: "Hybrid" },
  INTERNSHIP: { icon: "school", label: "Internship" },
};
const EXP_MAP = { FRESHER: "Entry", JUNIOR: "Junior", MID: "Mid-level", SENIOR: "Senior" };

export function JobTypePill({ type }) {
  const info = TYPE_MAP[type] || { icon: "work", label: type };
  return <span className="pill"><Icon name={info.icon} size={13} />{info.label}</span>;
}

export function ExperiencePill({ level }) {
  return <span className="pill"><Icon name="stairs" size={13} />{EXP_MAP[level] || level}</span>;
}
