import { formatarDataHora } from "../../../../utils/formato";
import {
  ROTULO_CANAL,
  ROTULO_STATUS_TENTATIVA,
  ROTULO_TIPO_DESTINATARIO,
  type StatusTentativa,
  type TentativaContatoDTO,
} from "../../tipos";

const COR_STATUS_TENTATIVA: Record<StatusTentativa, string> = {
  EXECUTADA_COM_SUCESSO: "bg-emerald-50 text-emerald-800 ring-emerald-200",
  SEM_RESPOSTA: "bg-amber-50 text-amber-800 ring-amber-200",
  FALHA_TECNICA: "bg-paw-50 text-paw-700 ring-paw-200",
};

/** Card de uma tentativa de contato (RN 3) — visão operacional da recepção. */
export function ItemTentativa({ tentativa }: { tentativa: TentativaContatoDTO }) {
  return (
    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-xl border border-ink-300/60 bg-white px-4 py-3 text-sm">
      <span className="font-medium text-ink-800">
        {ROTULO_TIPO_DESTINATARIO[tentativa.tipoDestinatario]}
      </span>
      <span className="chip">{ROTULO_CANAL[tentativa.canal]}</span>
      <span
        className={
          "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold ring-1 " +
          COR_STATUS_TENTATIVA[tentativa.status]
        }
      >
        {ROTULO_STATUS_TENTATIVA[tentativa.status]}
      </span>
      <time className="text-xs text-ink-500" dateTime={tentativa.executadaEm}>
        {formatarDataHora(tentativa.executadaEm)}
      </time>
      {tentativa.mensagemRetorno && (
        <span className="w-full text-xs text-ink-500">“{tentativa.mensagemRetorno}”</span>
      )}
    </div>
  );
}
