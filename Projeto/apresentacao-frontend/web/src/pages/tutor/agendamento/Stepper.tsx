/** Indicador visual de passos do wizard (passo atual + concluídos). */
export function Stepper({ passos, atual }: { passos: string[]; atual: number }) {
  return (
    <ol className="mb-8 flex flex-wrap items-center gap-y-3">
      {passos.map((rotulo, i) => {
        const concluido = i < atual;
        const ativo = i === atual;
        return (
          <li key={rotulo} className="flex items-center">
            <div className="flex items-center gap-2">
              <span
                className={
                  "flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold transition " +
                  (ativo
                    ? "bg-brand-500 text-white shadow-card"
                    : concluido
                      ? "bg-brand-100 text-brand-700"
                      : "bg-ink-100 text-ink-500")
                }
                aria-current={ativo ? "step" : undefined}
              >
                {concluido ? "✓" : i + 1}
              </span>
              <span
                className={
                  "hidden text-sm font-medium sm:inline " +
                  (ativo ? "text-ink-900" : concluido ? "text-ink-700" : "text-ink-500")
                }
              >
                {rotulo}
              </span>
            </div>
            {i < passos.length - 1 && (
              <span className={"mx-2 h-px w-6 sm:w-10 " + (concluido ? "bg-brand-300" : "bg-ink-300")} />
            )}
          </li>
        );
      })}
    </ol>
  );
}
