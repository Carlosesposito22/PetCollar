import { formatarDataHora } from "../../../../utils/formato";
import {
  montarTimeline,
  tempoRelativo,
  type EventoEscalonamentoDTO,
  type EventoTimeline,
  type TentativaContatoDTO,
} from "../../tipos";
import { BadgeCriticidade } from "./BadgeCriticidade";

type Props = {
  tentativas: TentativaContatoDTO[];
  eventos: EventoEscalonamentoDTO[];
  modo?: "compacto" | "completo";
};

const COR_PONTO: Record<EventoTimeline["criticidade"], string> = {
  BAIXA: "bg-ink-300",
  MEDIA: "bg-amber-400",
  ALTA: "bg-orange-500",
  CRITICA: "bg-paw-500",
};

const ICONE: Record<EventoTimeline["tipo"], string> = {
  TENTATIVA: "📞",
  ESCALONAMENTO: "⬆️",
};

/**
 * Linha do tempo vertical (mais recente primeiro) com tentativas e escalonamentos.
 * `compacto` (card do tutor) limita aos 5 eventos mais recentes; `completo`
 * (recepção) mostra tudo.
 */
export function TimelineProtocolo({ tentativas, eventos, modo = "completo" }: Props) {
  const todos = montarTimeline(tentativas, eventos);
  const itens = modo === "compacto" ? todos.slice(0, 5) : todos;

  if (itens.length === 0) {
    return (
      <p className="px-1 py-4 text-sm text-ink-500">
        Ainda não há eventos registrados neste protocolo.
      </p>
    );
  }

  return (
    <ol className="relative space-y-4 pl-6">
      <span aria-hidden className="absolute left-[7px] top-1 bottom-1 w-px bg-ink-300/70" />
      {itens.map((ev) => (
        <li key={ev.tipo + ev.id} className="relative">
          <span
            aria-hidden
            className={"absolute -left-[22px] top-1 h-3.5 w-3.5 rounded-full ring-2 ring-white " + COR_PONTO[ev.criticidade]}
          />
          <div className="flex flex-wrap items-center gap-2">
            <span aria-hidden>{ICONE[ev.tipo]}</span>
            <span className="text-sm font-medium text-ink-800">{ev.titulo}</span>
            <BadgeCriticidade criticidade={ev.criticidade} />
          </div>
          {ev.detalhe && <p className="mt-0.5 text-xs text-ink-500">{ev.detalhe}</p>}
          <time
            className="text-xs text-ink-500"
            dateTime={ev.timestamp}
            title={formatarDataHora(ev.timestamp)}
          >
            {tempoRelativo(ev.timestamp)}
          </time>
        </li>
      ))}
      {modo === "compacto" && todos.length > itens.length && (
        <li className="text-xs text-ink-500">+ {todos.length - itens.length} evento(s) anteriores</li>
      )}
    </ol>
  );
}
