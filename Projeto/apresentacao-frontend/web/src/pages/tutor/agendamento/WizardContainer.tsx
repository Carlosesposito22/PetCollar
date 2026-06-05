import type { ReactNode } from "react";
import { Stepper } from "./Stepper";

/**
 * Shell de wizard: cabeçalho com título, stepper visual e barra de navegação
 * (Voltar / ação primária). O conteúdo do passo é renderizado como children.
 */
export function WizardContainer({
  titulo,
  subtitulo,
  passos,
  passoAtual,
  children,
  onVoltar,
  onAvancar,
  rotuloAvancar = "Continuar",
  avancarHabilitado,
  avancando = false,
}: {
  titulo: string;
  subtitulo?: string;
  passos: string[];
  passoAtual: number;
  children: ReactNode;
  onVoltar?: () => void;
  onAvancar: () => void;
  rotuloAvancar?: string;
  avancarHabilitado: boolean;
  avancando?: boolean;
}) {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-ink-900">{titulo}</h1>
        {subtitulo && <p className="text-sm text-ink-500">{subtitulo}</p>}
      </div>

      <Stepper passos={passos} atual={passoAtual} />

      <div className="card p-6">{children}</div>

      <div className="mt-6 flex items-center justify-between gap-3">
        <button
          type="button"
          onClick={onVoltar}
          disabled={!onVoltar}
          className="btn-ghost ring-1 ring-ink-300 disabled:invisible"
        >
          ← Voltar
        </button>
        <button
          type="button"
          onClick={onAvancar}
          disabled={!avancarHabilitado || avancando}
          aria-busy={avancando}
          className="btn-primary w-auto"
        >
          {avancando ? "Processando…" : rotuloAvancar}
        </button>
      </div>
    </div>
  );
}
