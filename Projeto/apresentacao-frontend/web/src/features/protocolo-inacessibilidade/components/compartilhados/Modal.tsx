import { useEffect, useRef, type ReactNode } from "react";

type Props = {
  aberto: boolean;
  titulo: string;
  onFechar: () => void;
  children: ReactNode;
  rodape?: ReactNode;
};

/**
 * Modal base com gestão de foco e acessibilidade: trava o scroll de fundo, fecha
 * com ESC, foca o primeiro controle ao abrir e devolve o foco ao elemento anterior
 * ao fechar. Usado por todas as confirmações destrutivas da F-03.
 */
export function Modal({ aberto, titulo, onFechar, children, rodape }: Props) {
  const painelRef = useRef<HTMLDivElement>(null);
  const focoAnteriorRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!aberto) return;
    focoAnteriorRef.current = document.activeElement as HTMLElement | null;
    const aoTeclar = (e: KeyboardEvent) => {
      if (e.key === "Escape") onFechar();
    };
    document.addEventListener("keydown", aoTeclar);
    const original = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    // Foca o primeiro elemento focável do painel.
    const focavel = painelRef.current?.querySelector<HTMLElement>(
      "input, textarea, select, button, [tabindex]:not([tabindex='-1'])",
    );
    focavel?.focus();
    return () => {
      document.removeEventListener("keydown", aoTeclar);
      document.body.style.overflow = original;
      focoAnteriorRef.current?.focus();
    };
  }, [aberto, onFechar]);

  if (!aberto) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-ink-900/40 p-4 backdrop-blur-sm sm:items-center"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onFechar();
      }}
    >
      <div
        ref={painelRef}
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className="card w-full max-w-md p-6"
      >
        <div className="mb-4 flex items-start justify-between gap-4">
          <h2 className="text-lg font-semibold text-ink-900">{titulo}</h2>
          <button onClick={onFechar} aria-label="Fechar" className="btn-ghost -mr-2 -mt-1">
            ✕
          </button>
        </div>
        <div className="space-y-4">{children}</div>
        {rodape && <div className="mt-6 flex justify-end gap-2">{rodape}</div>}
      </div>
    </div>
  );
}
