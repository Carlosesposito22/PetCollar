import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../../services/protocoloService";
import { BannerErro, Skeleton } from "../../components/compartilhados/Primitivos";
import { PollingIndicator } from "../../components/compartilhados/PollingIndicator";
import { ToastProvider, useToast } from "../../components/compartilhados/Toast";
import { FiltrosProtocolos } from "../../components/recepcionista/FiltrosProtocolos";
import { ModalAtivacaoManual } from "../../components/recepcionista/ModalAtivacaoManual";
import { ModalEncerramentoProtocolo } from "../../components/recepcionista/ModalEncerramentoProtocolo";
import { TabelaProtocolosAtivos } from "../../components/recepcionista/TabelaProtocolosAtivos";
import { useAcoesProtocolo } from "../../hooks/useAcoesProtocolo";
import { useProtocolosAtivos } from "../../hooks/useProtocolosAtivos";
import { protocoloStore, useFiltrosProtocolos } from "../../store/protocoloStore";
import type { StatusProtocolo, StatusProtocoloDTO } from "../../tipos";

export function PainelProtocolosAtivosPage() {
  return (
    <ToastProvider>
      <PainelInterno />
    </ToastProvider>
  );
}

function PainelInterno() {
  const navigate = useNavigate();
  const toast = useToast();
  const { dados, carregando, atualizando, erro, ultimaAtualizacao, recarregar } =
    useProtocolosAtivos();
  const filtros = useFiltrosProtocolos();
  const { ativarManualmente, encerrar, executando } = useAcoesProtocolo();

  const [modalAtivar, setModalAtivar] = useState(false);
  const [alvoEncerrar, setAlvoEncerrar] = useState<StatusProtocoloDTO | null>(null);
  const [erroAcao, setErroAcao] = useState<string | null>(null);

  const lista = dados ?? [];

  const metricas = useMemo(() => {
    const conta = (s: StatusProtocolo) => lista.filter((p) => p.status === s).length;
    return {
      total: lista.length,
      tutor: conta("EM_TENTATIVA_TUTOR") + conta("ATIVADO"),
      secundarios: conta("EM_TENTATIVA_SECUNDARIOS"),
      escalonamento: conta("EM_ESCALONAMENTO"),
    };
  }, [lista]);

  const filtrados = useMemo(() => {
    const busca = filtros.busca.trim().toLowerCase();
    const inicio = filtros.inicio ? new Date(filtros.inicio + "T00:00:00").getTime() : null;
    const fim = filtros.fim ? new Date(filtros.fim + "T23:59:59").getTime() : null;
    return lista.filter((p) => {
      if (filtros.status.length > 0 && !filtros.status.includes(p.status)) return false;
      if (busca) {
        const alvo = `${p.id} ${p.atendimentoId} ${p.pacienteId}`.toLowerCase();
        if (!alvo.includes(busca)) return false;
      }
      if (p.ativadoEm) {
        const t = new Date(p.ativadoEm).getTime();
        if (inicio != null && t < inicio) return false;
        if (fim != null && t > fim) return false;
      }
      return true;
    });
  }, [lista, filtros]);

  async function confirmarAtivacao(atendimentoId: string) {
    setErroAcao(null);
    try {
      await ativarManualmente(atendimentoId);
      toast.sucesso("Protocolo ativado manualmente.");
      setModalAtivar(false);
      recarregar();
    } catch (e) {
      setErroAcao(e instanceof ApiError ? e.message : "Falha ao ativar o protocolo.");
    }
  }

  async function confirmarEncerramento(detalhes: string) {
    if (!alvoEncerrar) return;
    setErroAcao(null);
    try {
      await encerrar(alvoEncerrar.id, detalhes);
      toast.sucesso("Protocolo encerrado.");
      setAlvoEncerrar(null);
      recarregar();
    } catch (e) {
      setErroAcao(e instanceof ApiError ? e.message : "Falha ao encerrar o protocolo.");
    }
  }

  return (
    <div className="mx-auto max-w-6xl space-y-5 p-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold text-ink-900">Protocolos de inacessibilidade</h1>
          <p className="text-sm text-ink-500">Acompanhamento operacional em tempo real.</p>
        </div>
        <PollingIndicator atualizando={atualizando} ativo ultimaAtualizacao={ultimaAtualizacao} />
      </header>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <CardMetrica titulo="Ativos" valor={metricas.total} cor="brand" onClick={() => protocoloStore.limparFiltros()} />
        <CardMetrica titulo="Com o tutor" valor={metricas.tutor} cor="amber" onClick={() => protocoloStore.setFiltros({ status: ["EM_TENTATIVA_TUTOR", "ATIVADO"] })} />
        <CardMetrica titulo="Secundários" valor={metricas.secundarios} cor="orange" onClick={() => protocoloStore.setFiltros({ status: ["EM_TENTATIVA_SECUNDARIOS"] })} />
        <CardMetrica titulo="Escalonamento" valor={metricas.escalonamento} cor="paw" onClick={() => protocoloStore.setFiltros({ status: ["EM_ESCALONAMENTO"] })} />
      </div>

      <FiltrosProtocolos />

      {erro && <BannerErro mensagem={erro.message} onTentarNovamente={recarregar} />}

      {carregando ? (
        <Skeleton className="h-48" />
      ) : (
        <TabelaProtocolosAtivos
          protocolos={filtrados}
          onAbrir={(id) => navigate(`/recepcao/protocolos/${id}`)}
          onEncerrar={(p) => {
            setErroAcao(null);
            setAlvoEncerrar(p);
          }}
        />
      )}

      <button
        onClick={() => {
          setErroAcao(null);
          setModalAtivar(true);
        }}
        className="btn-primary fixed bottom-6 right-6 z-40 w-auto shadow-card"
      >
        + Ativar manualmente
      </button>

      <ModalAtivacaoManual
        aberto={modalAtivar}
        executando={executando}
        erro={modalAtivar ? erroAcao : null}
        onFechar={() => setModalAtivar(false)}
        onConfirmar={confirmarAtivacao}
      />
      <ModalEncerramentoProtocolo
        alvo={alvoEncerrar}
        executando={executando}
        erro={alvoEncerrar ? erroAcao : null}
        onFechar={() => setAlvoEncerrar(null)}
        onConfirmar={confirmarEncerramento}
      />
    </div>
  );
}

const COR_METRICA = {
  brand: "ring-brand-200 text-brand-700",
  amber: "ring-amber-200 text-amber-800",
  orange: "ring-orange-200 text-orange-800",
  paw: "ring-paw-200 text-paw-700",
} as const;

function CardMetrica({
  titulo,
  valor,
  cor,
  onClick,
}: {
  titulo: string;
  valor: number;
  cor: keyof typeof COR_METRICA;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={"card flex flex-col items-start p-4 text-left ring-1 transition hover:shadow-lg " + COR_METRICA[cor]}
    >
      <span className="text-xs font-medium uppercase tracking-wide text-ink-500">{titulo}</span>
      <span className="mt-1 text-2xl font-bold">{valor}</span>
    </button>
  );
}
