/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  "#eefcf6",
          100: "#d6f6e7",
          200: "#aeecd0",
          300: "#7adcb4",
          400: "#43c694",
          500: "#1eaa78",
          600: "#138a62",
          700: "#106e50",
          800: "#0f5841",
          900: "#0c4836",
        },
        ink: {
          900: "#0b1220",
          800: "#111827",
          700: "#1f2937",
          500: "#64748b",
          300: "#cbd5e1",
          100: "#f1f5f9",
        },
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "Segoe UI", "Roboto", "sans-serif"],
      },
      boxShadow: {
        card: "0 10px 30px -15px rgba(15, 88, 65, 0.25)",
      },
    },
  },
  plugins: [],
};
