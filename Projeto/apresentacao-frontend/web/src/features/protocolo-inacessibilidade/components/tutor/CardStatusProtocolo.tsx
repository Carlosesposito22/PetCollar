import { formatarDataHora } from "../../../../utils/formato";
import { isProtocoloAtivo, ROTULO_NIVEL, type VisaoProtocoloDTO } from "../../tipos";
import { BadgeStatusProtocolo } from "../compartilhados/BadgeStatusProtocolo";
import { PollingIndicator } from "../compartilhados/PollingIndicator";

type Props = {
  visao: VisaoProtocoloDTO;
  atualizando: boolean;
  ultimaAtualizacao: number | null;
};

/** Card principal de status para o tutor — resumido e tranquilizador (RN 15). */
export function CardStatusProtocolo({ visao, atualizando, ultimaAtualizacao }: Props) {
  const ativo = isProtocoloAtivo(visao.status);
  return (
    <div className="card p-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-2">
          <BadgeStatusProtocolo status={visao.status} tamanho="lg" />
          {visao.nivelEscalonamentoAtual && (
            <p className="text-sm text-ink-500">{ROTULO_NIVEL[visao.nivelEscalonamentoAtual]}</p>
          )}
        </div>
        <PollingIndicator atualizando={atualizando} ativo={ativo} ultimaAtualizacao={ultimaAtualizacao} />
      </div>

      <dl className="mt-5 grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-ink-500">Atendimento</dt>
          <dd className="font-medium text-ink-800">{visao.atendimentoId}</dd>
        </div>
        <div>
          <dt className="text-ink-500">Protocolo iniciado em</dt>
          <dd className="font-medium text-ink-800">{formatarDataHora(visao.ativadoEm)}</dd>
        </div>
      </dl>
    </div>
  );
}
