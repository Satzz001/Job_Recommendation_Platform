/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        cream: "#FCF3E1",
        "cream-2": "#F6ECD9",
        "cream-3": "#EFE1C8",
        ink: "#18140D",
        muted: "#5c5645",
        faint: "#8a8266",
      },
      fontFamily: {
        sans: ["Plus Jakarta Sans", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "monospace"],
      },
      borderWidth: { 3: "3px" },
      borderRadius: { xl2: "20px" },
      boxShadow: {
        "brutal-sm": "3px 3px 0 var(--border)",
        brutal: "5px 5px 0 var(--border)",
        "brutal-lg": "7px 7px 0 var(--border)",
      },
    },
  },
  plugins: [],
};
