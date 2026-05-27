/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        // Identidade petCollar — azul/turquesa (confiança, compromisso)
        brand: {
          50:  "#e7fbfc",
          100: "#c5f4f7",
          200: "#92e9ef",
          300: "#54d6e0",
          400: "#1fbecb",
          500: "#02aab5", // cor oficial #02AAB5
          600: "#018f99",
          700: "#06717b",
          800: "#0b5a62",
          900: "#0d4a52",
        },
        // Identidade petCollar — vermelho (amor, paixão) — usado na pata e acentos
        paw: {
          50:  "#fff0f3",
          100: "#ffdde3",
          200: "#ffc0cc",
          300: "#ff94a8",
          400: "#fa5575",
          500: "#e0133a", // cor oficial #E0133A
          600: "#c20f31",
          700: "#a30d2a",
          800: "#871024",
          900: "#721022",
        },
        ink: {
          900: "#1f1f1f",
          800: "#2e2e2e",
          700: "#4a4a4a", // cinza escuro oficial #4A4A4A
          500: "#6b7280",
          300: "#cdcdcd",
          100: "#f3f5f6",
        },
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "Segoe UI", "Roboto", "sans-serif"],
      },
      boxShadow: {
        card: "0 10px 30px -15px rgba(2, 170, 181, 0.25)",
      },
    },
  },
  plugins: [],
};
