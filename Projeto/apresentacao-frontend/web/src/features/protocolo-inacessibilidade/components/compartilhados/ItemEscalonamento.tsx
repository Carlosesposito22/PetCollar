import { formatarDataHora } from "../../../../utils/formato";
import { CRITICIDADE_DO_NIVEL, ROTULO_NIVEL, type EventoEscalonamentoDTO } from "../../tipos";
import { BadgeCriticidade } from "./BadgeCriticidade";

/** Card de um evento de escalonamento auditável (RN 6/7). */
export function ItemEscalonamento({ evento }: { evento: EventoEscalonamentoDTO }) {
  return (
    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-xl border border-ink-300/60 bg-white px-4 py-3 text-sm">
      <span className="font-medium text-ink-800">{ROTULO_NIVEL[evento.nivel]}</span>
      <BadgeCriticidade criticidade={CRITICIDADE_DO_NIVEL[evento.nivel]} />
      <time className="text-xs text-ink-500" dateTime={evento.ocorridoEm}>
        {formatarDataHora(evento.ocorridoEm)}
      </time>
      {evento.responsavelAcionadoId && (
        <span className="text-xs text-ink-500">
          Responsável: {evento.responsavelAcionadoId}
        </span>
      )}
      {evento.motivo && <span className="w-full text-xs text-ink-500">{evento.motivo}</span>}
    </div>
  );
}
