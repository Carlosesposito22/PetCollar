import { useMemo, useState } from "react";
import { formatarDataHora } from "../../../../utils/formato";
import {
  COR_CRITICIDADE,
  ROTULO_CRITICIDADE,
  type NivelCriticidade,
  type NotificacaoProtocoloDTO,
} from "../../tipos";
import { BadgeCriticidade } from "./BadgeCriticidade";

type FiltroCriticidade = NivelCriticidade | "TODAS";

/**
 * Histórico auditável de notificações enviadas pelo protocolo (RN 16), ordenado
 * do mais recente ao mais antigo, com filtro local por criticidade.
 */
export function ListaNotificacoesProtocolo({
  notificacoes,
}: {
  notificacoes: NotificacaoProtocoloDTO[];
}) {
  const [filtro, setFiltro] = useState<FiltroCriticidade>("TODAS");

  const filtradas = useMemo(() => {
    return notificacoes.filter((n) => filtro === "TODAS" || n.criticidade === filtro);
  }, [notificacoes, filtro]);

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-ink-500">
          {notificacoes.length} notificação{notificacoes.length !== 1 ? "ões" : ""} registrada
          {notificacoes.length !== 1 ? "s" : ""}.
        </p>
        <label className="text-sm">
          <span className="mr-2 text-ink-500">Criticidade:</span>
          <select
            className="rounded-lg border border-ink-300 bg-white px-2 py-1 text-sm"
            value={filtro}
            onChange={(e) => setFiltro(e.target.value as FiltroCriticidade)}
          >
            <option value="TODAS">Todas</option>
            {(Object.keys(ROTULO_CRITICIDADE) as NivelCriticidade[]).map((c) => (
              <option key={c} value={c}>
                {ROTULO_CRITICIDADE[c]}
              </option>
            ))}
          </select>
        </label>
      </div>

      {filtradas.length === 0 ? (
        <p className="text-sm text-ink-500">
          {notificacoes.length === 0
            ? "Nenhuma notificação registrada para este protocolo ainda."
            : "Nenhuma notificação para o filtro selecionado."}
        </p>
      ) : (
        <div className="space-y-2">
          {filtradas.map((n) => (
            <ItemNotificacao key={n.id} notificacao={n} />
          ))}
        </div>
      )}
    </div>
  );
}

function ItemNotificacao({ notificacao: n }: { notificacao: NotificacaoProtocoloDTO }) {
  const [expandido, setExpandido] = useState(false);

  return (
    <div
      className={
        "rounded-xl border p-4 transition " +
        (COR_CRITICIDADE[n.criticidade]
          ? "border-ink-200"
          : "border-ink-200")
      }
    >
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <BadgeCriticidade criticidade={n.criticidade} />
            <span className="text-sm font-medium text-ink-800">{n.titulo}</span>
          </div>
          <p className="mt-1 text-xs text-ink-500">
            Destinatário: <span className="font-mono">{n.destinatarioId}</span>
          </p>
        </div>
        <time className="shrink-0 text-xs text-ink-400">
          {formatarDataHora(n.registradoEm)}
        </time>
      </div>

      {expandido && (
        <p className="mt-3 rounded-lg bg-ink-50 px-3 py-2 text-sm text-ink-700">{n.corpo}</p>
      )}

      <button
        onClick={() => setExpandido((prev) => !prev)}
        className="mt-2 text-xs text-brand-600 hover:underline"
      >
        {expandido ? "Ocultar mensagem" : "Ver mensagem"}
      </button>
    </div>
  );
}
