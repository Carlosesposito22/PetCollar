type Tone = "dark" | "light";

/** Pata da marca petCollar (vermelho #E0133A por padrão via text-paw-500). */
export function Paw({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 64 64" className={className} fill="currentColor" aria-hidden>
      <ellipse cx="32" cy="45" rx="14" ry="11" />
      <circle cx="13" cy="31" r="6.5" />
      <circle cx="25.5" cy="19" r="6.5" />
      <circle cx="38.5" cy="19" r="6.5" />
      <circle cx="51" cy="31" r="6.5" />
    </svg>
  );
}

/** Selo quadrado com a pata — usado como ícone/avatar da marca. */
export function BrandMark({ size = 40 }: { size?: number }) {
  return (
    <div
      className="inline-flex items-center justify-center rounded-2xl bg-brand-500 shadow-card"
      style={{ width: size, height: size }}
      aria-hidden
    >
      <Paw className="text-white" />
    </div>
  );
}

/**
 * Wordmark "PET CØLLAR" — o "O" de COLLAR é substituído pela pata vermelha,
 * fiel ao manual de identidade visual.
 */
export function BrandWordmark({ tone = "dark", size = "md" }: { tone?: Tone; size?: "md" | "lg" }) {
  const texto = tone === "light" ? "text-white" : "text-brand-600";
  const subtitulo = tone === "light" ? "text-white/80" : "text-ink-500";
  const escala = size === "lg" ? "text-3xl" : "text-2xl";
  const pataAltura = size === "lg" ? "h-6 w-6" : "h-5 w-5";

  return (
    <div className="flex items-center gap-2.5">
      <span className={`font-extrabold leading-none tracking-tight ${escala} ${texto}`}>
        <span>PET</span>
        <span className="ml-1.5 inline-flex items-center">
          C
          <Paw className={`mx-[1px] text-paw-500 ${pataAltura}`} />
          LLAR
        </span>
      </span>
      {size === "lg" && (
        <span className={`ml-1 hidden text-[11px] font-medium leading-tight sm:block ${subtitulo}`}>
          Fortalecendo o vínculo<br />entre pessoas e animais
        </span>
      )}
    </div>
  );
}
