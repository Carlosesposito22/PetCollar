import type { ReactNode } from "react";

/** Bloco de carregamento (shimmer) reutilizável. */
export function Skeleton({ className = "" }: { className?: string }) {
  return <div className={"animate-pulse rounded-lg bg-ink-100 " + className} aria-hidden />;
}

/** Estado vazio amigável (ex.: 404 = sem protocolo ativo, não é erro). */
export function EstadoVazio({
  titulo,
  descricao,
  icone = "🐾",
  acao,
}: {
  titulo: string;
  descricao?: string;
  icone?: string;
  acao?: ReactNode;
}) {
  return (
    <div className="card flex flex-col items-center gap-2 px-6 py-12 text-center">
      <span className="text-3xl" aria-hidden>
        {icone}
      </span>
      <h3 className="text-base font-semibold text-ink-800">{titulo}</h3>
      {descricao && <p className="max-w-md text-sm text-ink-500">{descricao}</p>}
      {acao && <div className="mt-2">{acao}</div>}
    </div>
  );
}

/** Banner de erro inline (rede / regra de negócio), com ação opcional de retry. */
export function BannerErro({
  mensagem,
  onTentarNovamente,
}: {
  mensagem: string;
  onTentarNovamente?: () => void;
}) {
  return (
    <div
      role="alert"
      className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-paw-200 bg-paw-50 px-4 py-3 text-sm text-paw-700"
    >
      <span className="flex items-center gap-2">
        <span aria-hidden>⚠</span>
        {mensagem}
      </span>
      {onTentarNovamente && (
        <button
          onClick={onTentarNovamente}
          className="rounded-lg bg-paw-500 px-3 py-1.5 text-xs font-semibold text-white hover:bg-paw-600"
        >
          Tentar novamente
        </button>
      )}
    </div>
  );
}
