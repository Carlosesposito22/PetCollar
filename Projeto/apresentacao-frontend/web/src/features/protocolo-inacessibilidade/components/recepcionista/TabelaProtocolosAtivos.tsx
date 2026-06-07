import { useMemo } from "react";
import { formatarDataHora } from "../../../../utils/formato";
import {
  criticidadeDoStatus,
  ROTULO_NIVEL,
  tempoRelativo,
  type NivelCriticidade,
  type StatusProtocoloDTO,
} from "../../tipos";
import { BadgeStatusProtocolo } from "../compartilhados/BadgeStatusProtocolo";

type Props = {
  protocolos: StatusProtocoloDTO[];
  onAbrir: (protocoloId: string) => void;
  onEncerrar: (protocolo: StatusProtocoloDTO) => void;
};

const PESO_CRITICIDADE: Record<NivelCriticidade, number> = {
  BAIXA: 0,
  MEDIA: 1,
  ALTA: 2,
  CRITICA: 3,
};

/** Tabela (desktop) / cards (mobile) dos protocolos ativos, criticidade ↓ e tempo ativo ↓. */
export function TabelaProtocolosAtivos({ protocolos, onAbrir, onEncerrar }: Props) {
  const ordenados = useMemo(() => {
    return [...protocolos].sort((a, b) => {
      const ca = PESO_CRITICIDADE[criticidadeDoStatus(a.status, a.nivelEscalonamentoAtual)];
      const cb = PESO_CRITICIDADE[criticidadeDoStatus(b.status, b.nivelEscalonamentoAtual)];
      if (cb !== ca) return cb - ca;
      const ta = a.ativadoEm ? new Date(a.ativadoEm).getTime() : 0;
      const tb = b.ativadoEm ? new Date(b.ativadoEm).getTime() : 0;
      return ta - tb; // mais antigo (mais tempo ativo) primeiro
    });
  }, [protocolos]);

  if (ordenados.length === 0) {
    return (
      <div className="card px-6 py-12 text-center text-sm text-ink-500">
        Nenhum protocolo ativo para os filtros selecionados.
      </div>
    );
  }

  return (
    <>
      {/* Desktop */}
      <div className="card hidden overflow-hidden md:block">
        <table className="w-full text-left text-sm">
          <thead className="bg-ink-100/60 text-xs uppercase tracking-wide text-ink-500">
            <tr>
              <th className="px-4 py-3">Paciente</th>
              <th className="px-4 py-3">Atendimento</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Tempo ativo</th>
              <th className="px-4 py-3 text-right">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-300/50">
            {ordenados.map((p) => (
              <tr
                key={p.id}
                className="cursor-pointer transition hover:bg-brand-50/40"
                onClick={() => onAbrir(p.id)}
              >
                <td className="px-4 py-3 font-medium text-ink-800">{p.pacienteId}</td>
                <td className="px-4 py-3 text-ink-700">{p.atendimentoId}</td>
                <td className="px-4 py-3">
                  <BadgeStatusProtocolo status={p.status} tamanho="sm" />
                  {p.nivelEscalonamentoAtual && (
                    <span className="ml-2 text-xs text-ink-500">
                      {ROTULO_NIVEL[p.nivelEscalonamentoAtual]}
                    </span>
                  )}
                </td>
                <td className="px-4 py-3 text-ink-700" title={formatarDataHora(p.ativadoEm)}>
                  {tempoRelativo(p.ativadoEm)}
                </td>
                <td className="px-4 py-3 text-right" onClick={(e) => e.stopPropagation()}>
                  <button onClick={() => onAbrir(p.id)} className="btn-ghost text-brand-700">
                    Ver detalhes
                  </button>
                  <button
                    onClick={() => onEncerrar(p)}
                    className="btn-ghost text-paw-600 hover:bg-paw-50"
                  >
                    Encerrar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile */}
      <div className="space-y-3 md:hidden">
        {ordenados.map((p) => (
          <div key={p.id} className="card space-y-2 p-4" onClick={() => onAbrir(p.id)}>
            <div className="flex items-center justify-between">
              <BadgeStatusProtocolo status={p.status} tamanho="sm" />
              <span className="text-xs text-ink-500">{tempoRelativo(p.ativadoEm)}</span>
            </div>
            <p className="text-sm font-medium text-ink-800">Paciente {p.pacienteId}</p>
            <p className="text-xs text-ink-500">Atendimento {p.atendimentoId}</p>
            <div className="flex gap-2 pt-1" onClick={(e) => e.stopPropagation()}>
              <button onClick={() => onAbrir(p.id)} className="btn-ghost ring-1 ring-ink-300">
                Ver detalhes
              </button>
              <button
                onClick={() => onEncerrar(p)}
                className="btn-ghost text-paw-600 ring-1 ring-paw-200"
              >
                Encerrar
              </button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
