import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { formatarDataHora } from "../../../../utils/formato";
import { ApiError } from "../../services/protocoloService";
import { BadgeStatusProtocolo } from "../../components/compartilhados/BadgeStatusProtocolo";
import { IndicadorEtapaAtual } from "../../components/compartilhados/IndicadorEtapaAtual";
import { ListaDiretivasConsentimento } from "../../components/compartilhados/ListaDiretivasConsentimento";
import { ListaEscalonamentos } from "../../components/compartilhados/ListaEscalonamentos";
import { ListaNotificacoesProtocolo } from "../../components/compartilhados/ListaNotificacoesProtocolo";
import { ListaTentativas } from "../../components/compartilhados/ListaTentativas";
import { BannerErro, EstadoVazio, Skeleton } from "../../components/compartilhados/Primitivos";
import { PollingIndicator } from "../../components/compartilhados/PollingIndicator";
import { TimelineProtocolo } from "../../components/compartilhados/TimelineProtocolo";
import { ToastProvider, useToast } from "../../components/compartilhados/Toast";
import { ModalEncerramentoProtocolo } from "../../components/recepcionista/ModalEncerramentoProtocolo";
import { useAcoesProtocolo } from "../../hooks/useAcoesProtocolo";
import { useDetalheProtocolo } from "../../hooks/useDetalheProtocolo";
import { ROTULO_NIVEL } from "../../tipos";

const ABAS = [
  { id: "geral", rotulo: "Visão geral" },
  { id: "tentativas", rotulo: "Tentativas" },
  { id: "escalonamentos", rotulo: "Escalonamentos" },
  { id: "notificacoes", rotulo: "Notificações" },
  { id: "diretivas", rotulo: "Diretivas" },
] as const;

type AbaId = (typeof ABAS)[number]["id"];

export function DetalheProtocoloPage() {
  return (
    <ToastProvider>
      <DetalheInterno />
    </ToastProvider>
  );
}

function DetalheInterno() {
  const { protocoloId = "" } = useParams();
  const toast = useToast();
  const { dados, carregando, atualizando, erro, ultimaAtualizacao, recarregar } =
    useDetalheProtocolo(protocoloId);
  const { encerrar, avancarEtapa, executando } = useAcoesProtocolo();

  const [aba, setAba] = useState<AbaId>("geral");
  const [modalEncerrar, setModalEncerrar] = useState(false);
  const [erroAcao, setErroAcao] = useState<string | null>(null);

  if (carregando) {
    return (
      <div className="mx-auto max-w-5xl space-y-4 p-6">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-64" />
      </div>
    );
  }

  if (erro) {
    return (
      <div className="mx-auto max-w-5xl p-6">
        <BannerErro mensagem={erro.message} onTentarNovamente={recarregar} />
      </div>
    );
  }

  if (!dados) {
    return (
      <div className="mx-auto max-w-5xl p-6">
        <EstadoVazio titulo="Protocolo não encontrado." />
      </div>
    );
  }

  const { resumo, tentativas, escalonamentos, notificacoes, diretivas } = dados;
  const ativo = resumo != null;

  async function handleAvancarEtapa() {
    setErroAcao(null);
    try {
      await avancarEtapa(protocoloId);
      toast.sucesso("Próxima tentativa acionada.");
      recarregar();
    } catch (e) {
      setErroAcao(e instanceof ApiError ? e.message : "Falha ao acionar a próxima tentativa.");
    }
  }

  async function confirmarEncerramento(detalhes: string) {
    setErroAcao(null);
    try {
      await encerrar(protocoloId, detalhes);
      toast.sucesso("Protocolo encerrado.");
      setModalEncerrar(false);
      recarregar();
    } catch (e) {
      setErroAcao(e instanceof ApiError ? e.message : "Falha ao encerrar o protocolo.");
    }
  }

  return (
    <div className="mx-auto max-w-5xl space-y-5 p-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <Link to="/recepcao/protocolos" className="text-sm text-brand-700 hover:underline">
            ← Protocolos ativos
          </Link>
          <h1 className="mt-1 text-xl font-semibold text-ink-900">
            Protocolo <span className="font-mono text-base text-ink-500">{protocoloId}</span>
          </h1>
        </div>
        <PollingIndicator atualizando={atualizando} ativo={ativo} ultimaAtualizacao={ultimaAtualizacao} />
      </div>

      {!ativo && (
        <div className="rounded-xl border border-ink-300 bg-ink-100/60 px-4 py-3 text-sm text-ink-700">
          Este protocolo não está mais ativo. Tentativas e escalonamentos abaixo permanecem
          disponíveis para auditoria.
        </div>
      )}

      {/* Abas */}
      <div className="flex flex-wrap gap-1 border-b border-ink-300/60">
        {ABAS.map((a) => (
          <button
            key={a.id}
            onClick={() => setAba(a.id)}
            className={
              "rounded-t-lg px-4 py-2 text-sm font-medium transition " +
              (aba === a.id
                ? "border-b-2 border-brand-500 text-brand-700"
                : "text-ink-500 hover:text-ink-800")
            }
          >
            {a.rotulo}
          </button>
        ))}
      </div>

      {aba === "geral" && (
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
          <div className="space-y-5 lg:col-span-2">
            <section className="card p-6">
              <h2 className="mb-4 text-sm font-semibold text-ink-700">Etapa atual</h2>
              {resumo ? (
                <IndicadorEtapaAtual status={resumo.status} />
              ) : (
                <p className="text-sm text-ink-500">Protocolo encerrado.</p>
              )}
            </section>
            <section className="card p-6">
              <h2 className="mb-4 text-sm font-semibold text-ink-700">Linha do tempo</h2>
              <TimelineProtocolo tentativas={tentativas} eventos={escalonamentos} modo="completo" />
            </section>
          </div>

          <aside className="card h-fit space-y-4 p-6">
            <h2 className="text-sm font-semibold text-ink-700">Dados do protocolo</h2>
            {resumo && (
              <>
                <div>
                  <BadgeStatusProtocolo status={resumo.status} />
                  {resumo.nivelEscalonamentoAtual && (
                    <p className="mt-1 text-xs text-ink-500">
                      {ROTULO_NIVEL[resumo.nivelEscalonamentoAtual]}
                    </p>
                  )}
                </div>
                <dl className="space-y-2 text-sm">
                  <Linha rotulo="Paciente" valor={resumo.pacienteId} />
                  <Linha rotulo="Atendimento" valor={resumo.atendimentoId} />
                  <Linha rotulo="Ativado em" valor={formatarDataHora(resumo.ativadoEm)} />
                </dl>
                <button
                  onClick={handleAvancarEtapa}
                  disabled={executando}
                  className="btn-primary w-full"
                >
                  {executando ? "Acionando..." : "Acionar próxima tentativa"}
                </button>
                <button
                  onClick={() => {
                    setErroAcao(null);
                    setModalEncerrar(true);
                  }}
                  disabled={executando}
                  className="btn-primary w-full bg-paw-500 hover:bg-paw-600"
                >
                  Encerrar manualmente
                </button>
                {erroAcao && (
                  <p className="text-xs text-red-600">{erroAcao}</p>
                )}
              </>
            )}
          </aside>
        </div>
      )}

      {aba === "tentativas" && (
        <section className="card p-6">
          <ListaTentativas tentativas={tentativas} />
        </section>
      )}

      {aba === "escalonamentos" && (
        <section className="card p-6">
          <ListaEscalonamentos eventos={escalonamentos} />
        </section>
      )}

      {aba === "notificacoes" && (
        <section className="card p-6">
          <h2 className="mb-4 text-sm font-semibold text-ink-700">
            Histórico de notificações
          </h2>
          <ListaNotificacoesProtocolo notificacoes={notificacoes} />
        </section>
      )}

      {aba === "diretivas" && (
        <section className="card p-6">
          <h2 className="mb-4 text-sm font-semibold text-ink-700">
            Diretivas de consentimento do tutor
          </h2>
          <ListaDiretivasConsentimento diretivas={diretivas} />
        </section>
      )}

      <ModalEncerramentoProtocolo
        alvo={modalEncerrar ? resumo : null}
        executando={executando}
        erro={modalEncerrar ? erroAcao : null}
        onFechar={() => setModalEncerrar(false)}
        onConfirmar={confirmarEncerramento}
      />
    </div>
  );
}

function Linha({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div className="flex justify-between gap-3">
      <dt className="text-ink-500">{rotulo}</dt>
      <dd className="text-right font-medium text-ink-800">{valor}</dd>
    </div>
  );
}
