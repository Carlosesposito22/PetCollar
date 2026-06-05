import { createContext, useCallback, useContext, useState, type ReactNode } from "react";

type ToastTipo = "sucesso" | "erro" | "info";
type ToastItem = { id: number; tipo: ToastTipo; texto: string };

type ToastApi = {
  sucesso: (texto: string) => void;
  erro: (texto: string) => void;
  info: (texto: string) => void;
};

const ToastCtx = createContext<ToastApi | null>(null);

const ESTILO: Record<ToastTipo, string> = {
  sucesso: "border-emerald-200 bg-emerald-50 text-emerald-900",
  erro: "border-paw-200 bg-paw-50 text-paw-700",
  info: "border-brand-200 bg-brand-50 text-brand-800",
};

const ICONE: Record<ToastTipo, string> = { sucesso: "✓", erro: "✕", info: "ℹ" };

let proximoId = 1;

/** Provider de toasts autossuficiente (sem dependências externas). */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [itens, setItens] = useState<ToastItem[]>([]);

  const remover = useCallback((id: number) => {
    setItens(atual => atual.filter(t => t.id !== id));
  }, []);

  const mostrar = useCallback((tipo: ToastTipo, texto: string) => {
    const id = proximoId++;
    setItens(atual => [...atual, { id, tipo, texto }]);
    window.setTimeout(() => remover(id), 4500);
  }, [remover]);

  const api: ToastApi = {
    sucesso: t => mostrar("sucesso", t),
    erro: t => mostrar("erro", t),
    info: t => mostrar("info", t),
  };

  return (
    <ToastCtx.Provider value={api}>
      {children}
      <div className="pointer-events-none fixed inset-x-0 bottom-0 z-[60] flex flex-col items-center gap-2 p-4 sm:items-end sm:p-6">
        {itens.map(t => (
          <div
            key={t.id}
            role="status"
            className={
              "pointer-events-auto flex max-w-sm items-start gap-3 rounded-xl border px-4 py-3 text-sm shadow-card ring-1 ring-black/5 " +
              ESTILO[t.tipo]
            }
          >
            <span aria-hidden className="mt-0.5 font-bold">{ICONE[t.tipo]}</span>
            <span className="flex-1">{t.texto}</span>
            <button
              onClick={() => remover(t.id)}
              aria-label="Fechar"
              className="opacity-70 transition hover:opacity-100"
            >
              ✕
            </button>
          </div>
        ))}
      </div>
    </ToastCtx.Provider>
  );
}

export function useToast(): ToastApi {
  const ctx = useContext(ToastCtx);
  if (!ctx) throw new Error("useToast deve ser usado dentro de <ToastProvider>");
  return ctx;
}
