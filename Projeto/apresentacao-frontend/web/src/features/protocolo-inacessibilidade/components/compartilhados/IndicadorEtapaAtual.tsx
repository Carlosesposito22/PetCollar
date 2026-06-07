import {
  ETAPAS_ORDENADAS,
  etapaDoStatus,
  isStatusTerminal,
  type StatusProtocolo,
} from "../../tipos";

type EstadoEtapa = "concluida" | "atual" | "futura";

/**
 * Stepper das três etapas progressivas do protocolo (Tutor → Responsáveis
 * Secundários → Escalonamento), reforçando a sequência da RN 4/RN 5. Etapa atual
 * em destaque; anteriores com check; futuras esmaecidas.
 */
export function IndicadorEtapaAtual({ status }: { status: StatusProtocolo }) {
  const terminal = isStatusTerminal(status);
  const etapaAtual = etapaDoStatus(status);
  const idxAtual = ETAPAS_ORDENADAS.findIndex((e) => e.etapa === etapaAtual);

  function estadoDe(indice: number): EstadoEtapa {
    if (terminal) return "concluida";
    if (idxAtual === -1) return "futura";
    if (indice < idxAtual) return "concluida";
    if (indice === idxAtual) return "atual";
    return "futura";
  }

  return (
    <ol className="flex items-center gap-2" aria-label="Etapas do protocolo">
      {ETAPAS_ORDENADAS.map((etapa, i) => {
        const estado = estadoDe(i);
        const corCirculo =
          estado === "atual"
            ? "bg-brand-500 text-white ring-4 ring-brand-100"
            : estado === "concluida"
              ? "bg-emerald-500 text-white"
              : "bg-ink-100 text-ink-500";
        const corTexto =
          estado === "futura" ? "text-ink-500" : "text-ink-800 font-medium";
        return (
          <li key={etapa.etapa} className="flex flex-1 items-center gap-2">
            <div className="flex items-center gap-2">
              <span
                className={
                  "flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold " +
                  corCirculo
                }
                aria-current={estado === "atual" ? "step" : undefined}
              >
                {estado === "concluida" ? "✓" : i + 1}
              </span>
              <span className={"text-sm " + corTexto}>{etapa.rotulo}</span>
            </div>
            {i < ETAPAS_ORDENADAS.length - 1 && (
              <span
                aria-hidden
                className={
                  "h-px flex-1 " + (estado === "concluida" ? "bg-emerald-300" : "bg-ink-300")
                }
              />
            )}
          </li>
        );
      })}
    </ol>
  );
}
