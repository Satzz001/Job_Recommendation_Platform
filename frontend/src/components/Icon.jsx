export default function Icon({ name, filled, size, className = "", style }) {
  return (
    <span
      className={`material-symbols-rounded ${filled ? "filled" : ""} ${className}`}
      style={{ fontSize: size, ...style }}
    >
      {name}
    </span>
  );
}
