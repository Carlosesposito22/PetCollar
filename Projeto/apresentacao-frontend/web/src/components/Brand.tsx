export function BrandMark({ size = 40 }: { size?: number }) {
  return (
    <div
      className="inline-flex items-center justify-center rounded-2xl bg-brand-600 text-brand-50 shadow-card"
      style={{ width: size, height: size }}
      aria-hidden
    >
      <svg viewBox="0 0 64 64" width={size * 0.6} height={size * 0.6} fill="currentColor">
        <path d="M22 38c0-5 4-9 10-9s10 4 10 9c0 4-3 7-10 7s-10-3-10-7Z" />
        <circle cx="22" cy="22" r="4" />
        <circle cx="32" cy="18" r="4" />
        <circle cx="42" cy="22" r="4" />
        <circle cx="16" cy="30" r="3" />
        <circle cx="48" cy="30" r="3" />
      </svg>
    </div>
  );
}

export function BrandWordmark() {
  return (
    <div className="flex items-center gap-3">
      <BrandMark />
      <div className="leading-tight">
        <div className="text-xl font-extrabold tracking-tight text-ink-900">
          pet<span className="text-brand-600">Collar</span>
        </div>
        <div className="text-[11px] font-medium uppercase tracking-widest text-ink-500">
          Inteligência clínica veterinária
        </div>
      </div>
    </div>
  );
}
