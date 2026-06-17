import { useCallback, useEffect, useState } from "react";
import { formatarDataHora } from "../../../../utils/formato";
import { BannerErro, EstadoVazio, Skeleton } from "../../components/compartilhados/Primitivos";
import { BannerAlertaProtocolo } from "../../components/tutor/BannerAlertaProtocolo";
import { CardStatusProtocolo } from "../../components/tutor/CardStatusProtocolo";
import { IndicadorEtapaAtual } from "../../components/compartilhados/IndicadorEtapaAtual";
import { TimelineProtocolo } from "../../components/compartilhados/TimelineProtocolo";
import { ListaNotificacoesProtocolo } from "../../components/compartilhados/ListaNotificacoesProtocolo";
import { useMeuProtocoloAtivo } from "../../hooks/useMeuProtocoloAtivo";
import { useProtocoloService } from "../../services/useProtocoloService";
import { isProtocoloAtivo } from "../../tipos";
import type { NotificacaoProtocoloDTO, VisaoProtocoloDTO } from "../../tipos";

/**
 * Área do tutor para acompanhamento do protocolo de inacessibilidade ativo (RN 15).
 * Mostra status, etapas, timeline e — crucialmente — as mensagens enviadas à ele
 * pela clínica durante o protocolo (RN 16).
 */
export function AvisosDaClinicaPage() {
  const { dados, carregando, atualizando, erro, ultimaAtualizacao, recarregar } =
    useMeuProtocoloAtivo();

  const service = useProtocoloService();
  const [notificacoes, setNotificacoes] = useState<NotificacaoProtocoloDTO[]>([]);
  const [carregandoNotif, setCarregandoNotif] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null);

  const carregarNotificacoes = useCallback(async (protocoloId: string) => {
    setCarregandoNotif(true);
    try {
      const lista = await service.listarNotificacoes(protocoloId);
      setNotificacoes(lista);
    } catch {
      // notificações são enriquecimento; falha silenciosa não bloqueia a tela
    } finally {
      setCarregandoNotif(false);
    }
  }, [service]);

  useEffect(() => {
    if (dados?.protocoloId) {
      void carregarNotificacoes(dados.protocoloId);
    } else {
      setNotificacoes([]);
    }
  }, [dados?.protocoloId, carregarNotificacoes]);

  async function confirmarPresenca(protocoloId: string) {
    setErroConfirmacao(null);
    setConfirmando(true);
    try {
      await service.confirmarPresenca(protocoloId);
      recarregar();
    } catch (e) {
      setErroConfirmacao((e as Error).message ?? "Falha ao confirmar presença.");
    } finally {
      setConfirmando(false);
    }
  }

  if (carregando) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-20" />
        <Skeleton className="h-40" />
      </div>
    );
  }

  if (erro) {
    return <BannerErro mensagem={erro.message} onTentarNovamente={recarregar} />;
  }

  if (!dados) {
    return (
      <EstadoVazio
        icone="🟢"
        titulo="Nenhum aviso da clínica no momento."
        descricao="Está tudo certo. Caso a clínica precise falar com você com urgência, você verá o acompanhamento por aqui."
      />
    );
  }

  const ativo = isProtocoloAtivo(dados.status);

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-ink-900">Avisos da clínica</h1>
        <p className="mt-1 text-sm text-ink-500">
          Acompanhe as tentativas de contato e mensagens enviadas pela clínica sobre o atendimento
          do seu pet.
        </p>
      </div>

      {ativo && <BannerAlertaProtocolo status={dados.status} />}

      {ativo && (
        <CardConfirmarPresenca
          visao={dados}
          confirmando={confirmando}
          erro={erroConfirmacao}
          onConfirmar={() => confirmarPresenca(dados.protocoloId)}
        />
      )}

      {dados.status === "ENCERRADO_COM_SUCESSO" && (
        <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 text-emerald-900">
          <p className="text-base font-semibold">✓ Contato resolvido</p>
          <p className="mt-1 text-sm">
            Obrigado por retornar o contato. O protocolo foi encerrado com sucesso em{" "}
            {formatarDataHora(dados.encerradoEm)}.
          </p>
        </div>
      )}

      {dados.status === "ENCERRADO_POR_ESGOTAMENTO" && (
        <div className="rounded-2xl border border-paw-200 bg-paw-50 px-5 py-4 text-paw-700">
          <p className="text-base font-semibold">A clínica está em contato direto</p>
          <p className="mt-1 text-sm">
            Não conseguimos falar com você pelos canais cadastrados. Nossa equipe seguirá tentando
            por outros meios. O atendimento do seu pet continua normalmente.
          </p>
        </div>
      )}

      <CardStatusProtocolo
        visao={dados}
        atualizando={atualizando}
        ultimaAtualizacao={ultimaAtualizacao}
      />

      <section className="card p-6">
        <h2 className="mb-4 text-sm font-semibold text-ink-700">Etapa atual</h2>
        <IndicadorEtapaAtual status={dados.status} />
      </section>

      {/* Mensagens enviadas pela clínica — coração do RN 15/16 na visão do tutor */}
      <section className="card p-6">
        <h2 className="mb-4 text-sm font-semibold text-ink-700">Mensagens enviadas a você</h2>
        {carregandoNotif ? (
          <Skeleton className="h-24" />
        ) : (
          <ListaNotificacoesProtocolo notificacoes={notificacoes} />
        )}
      </section>

      <section className="card p-6">
        <h2 className="mb-4 text-sm font-semibold text-ink-700">Histórico de contatos</h2>
        <TimelineProtocolo
          tentativas={dados.tentativas}
          eventos={dados.eventosEscalonamento}
          modo="compacto"
        />
      </section>
    </div>
  );
}

type CardProps = {
  visao: VisaoProtocoloDTO;
  confirmando: boolean;
  erro: string | null;
  onConfirmar: () => void;
};

function CardConfirmarPresenca({ visao, confirmando, erro, onConfirmar }: CardProps) {
  return (
    <div className="rounded-2xl border-2 border-brand-300 bg-white p-6 shadow-sm">
      <div className="flex items-start gap-4">
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-brand-100 text-2xl">
          📍
        </div>
        <div className="flex-1">
          <h2 className="text-base font-bold text-ink-900">
            A clínica está tentando entrar em contato com você
          </h2>
          <p className="mt-1 text-sm text-ink-600">
            Se você está presente na clínica ou conseguiu receber o recado, confirme sua
            presença para que o atendimento do seu pet prossiga normalmente.
          </p>
          <p className="mt-1 text-xs text-ink-400">
            Atendimento: <span className="font-mono">{visao.atendimentoId}</span>
          </p>

          {erro && (
            <p className="mt-3 text-sm font-medium text-red-600">{erro}</p>
          )}

          <button
            onClick={onConfirmar}
            disabled={confirmando}
            className="btn-primary mt-4 w-full sm:w-auto"
          >
            {confirmando ? "Confirmando..." : "✓ Confirmar que estou presente"}
          </button>
        </div>
      </div>
    </div>
  );
}
