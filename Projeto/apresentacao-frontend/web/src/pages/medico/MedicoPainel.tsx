import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  type AtendimentoDoDiaDTO,
  type FilaItemDTO,
  type MedicoService,
} from "./medicoService";

const INTERVALO_POLLING_MS = 15_000;

export function MedicoPainel() {
  const { apiFetch, session } = useAuth();
  const navigate = useNavigate();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  // ── Fila de espera (pacientes encaminhados para este médico) ──────────────
  const [fila, setFila] = useState<FilaItemDTO[]>([]);
  const [carregandoFila, setCarregandoFila] = useState(true);
  const [erroFila, setErroFila] = useState<string | null>(null);

  const carregarFila = useCallback(async () => {
    try {
      const dados = await service.listarFilaDeEspera();
      setFila(dados);
      setErroFila(null);
    } catch (e) {
      setErroFila((e as Error).message);
    } finally {
      setCarregandoFila(false);
    }
  }, [service]);

  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);
  useEffect(() => {
    void carregarFila();
    pollingRef.current = setInterval(() => { void carregarFila(); }, INTERVALO_POLLING_MS);
    return () => { if (pollingRef.current) clearInterval(pollingRef.current); };
  }, [carregarFila]);

  // ── Consultas agendadas ───────────────────────────────────────────────────
  const [atendimentos, setAtendimentos] = useState<AtendimentoDoDiaDTO[]>([]);
  const [carregandoAtend, setCarregandoAtend] = useState(true);

  const recarregarAtendimentos = useCallback(() => {
    service.listarAtendimentosDoDia()
      .then(setAtendimentos)
      .finally(() => setCarregandoAtend(false));
  }, [service]);

  useEffect(() => { recarregarAtendimentos(); }, [recarregarAtendimentos]);

  // ── Modal de finalização de consulta ─────────────────────────────────────
  const [consultaParaFinalizar, setConsultaParaFinalizar] =
    useState<AtendimentoDoDiaDTO | null>(null);

  const medicoNome = session?.user.nome ?? session?.user.identificador ?? "Médico";

  return (
    <div>
      <div className="mb-6">
        <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">
          Painel do Médico Veterinário
        </p>
        <h1 className="mt-1 text-2xl font-bold text-ink-900">{medicoNome}</h1>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">

        {/* ── Painel esquerdo: Encaminhados pela recepção ─────────────────── */}
        <section className="card flex flex-col p-0 overflow-hidden">
          <div className="border-b border-ink-200/60 px-6 py-4">
            <h2 className="text-base font-semibold text-ink-900">
              Pacientes Encaminhados
            </h2>
            <p className="mt-0.5 text-xs text-ink-500">
              Encaminhados pela recepção · atualizado a cada 15 s
            </p>
          </div>

          <div className="flex-1 divide-y divide-ink-100">
            {carregandoFila ? (
              <div className="space-y-3 p-6">
                {[0, 1, 2].map((i) => (
                  <div key={i} className="h-16 animate-pulse rounded-xl bg-ink-100" />
                ))}
              </div>
            ) : erroFila ? (
              <div className="p-6">
                <div
                  role="alert"
                  className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900"
                >
                  {erroFila}
                </div>
              </div>
            ) : fila.length === 0 ? (
              <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
                <span className="mb-2 text-3xl">🐾</span>
                <p className="text-sm text-ink-500">
                  Nenhum paciente encaminhado no momento.
                </p>
              </div>
            ) : (
              fila.map((item) => (
                <FilaItem
                  key={item.triagemId}
                  item={item}
                  onAtender={() => navigate(`/medico/prontuario/${item.pacienteId}`)}
                />
              ))
            )}
          </div>
        </section>

        {/* ── Painel direito: Consultas agendadas ─────────────────────────── */}
        <section className="card flex flex-col p-0 overflow-hidden">
          <div className="border-b border-ink-200/60 px-6 py-4">
            <h2 className="text-base font-semibold text-ink-900">Consultas Agendadas</h2>
            <p className="mt-0.5 text-xs text-ink-500">
              Consultas que os tutores agendaram com você
            </p>
          </div>

          <div className="flex-1 overflow-x-auto">
            {carregandoAtend ? (
              <div className="space-y-3 p-6">
                {[0, 1, 2].map((i) => (
                  <div key={i} className="h-12 animate-pulse rounded-xl bg-ink-100" />
                ))}
              </div>
            ) : atendimentos.length === 0 ? (
              <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
                <span className="mb-2 text-3xl">📋</span>
                <p className="text-sm text-ink-500">Nenhuma consulta agendada.</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-ink-100 bg-ink-50/50">
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">
                      Horário
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">
                      Paciente
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">
                      Status
                    </th>
                    <th className="px-6 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100">
                  {atendimentos.map((a, i) => (
                    <AtendimentoLinha
                      key={i}
                      atendimento={a}
                      onFinalizar={() => setConsultaParaFinalizar(a)}
                    />
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>
      </div>

      <div className="mt-8 rounded-3xl border border-dashed border-ink-300/70 bg-white/90 p-5 text-sm text-ink-600">
        Painel esquerdo: pacientes encaminhados pela recepcionista para este médico.
        Painel direito: consultas agendadas previamente pelos tutores.
      </div>

      {consultaParaFinalizar && (
        <ModalFinalizarConsulta
          atendimento={consultaParaFinalizar}
          service={service}
          onFechado={() => setConsultaParaFinalizar(null)}
          onFinalizado={() => {
            setConsultaParaFinalizar(null);
            recarregarAtendimentos();
          }}
        />
      )}
    </div>
  );
}

// ── Subcomponentes ────────────────────────────────────────────────────────────

function FilaItem({ item, onAtender }: { item: FilaItemDTO; onAtender: () => void }) {
  return (
    <div className="flex items-center justify-between gap-4 px-6 py-4">
      <div className="min-w-0">
        <p className="font-semibold text-ink-900 truncate">
          {item.nomePaciente || `Paciente #${item.pacienteId.slice(0, 8)}`}
        </p>
        <div className="mt-1 flex flex-wrap items-center gap-2">
          <BadgeCorDeRisco cor={item.corDeRisco} />
          {item.finalizadaEm && (
            <span className="text-xs text-ink-400">
              {new Date(item.finalizadaEm).toLocaleTimeString("pt-BR", {
                hour: "2-digit", minute: "2-digit",
              })}
            </span>
          )}
        </div>
      </div>
      <button
        onClick={onAtender}
        className="btn-primary w-auto shrink-0 px-4 py-2 text-sm"
      >
        Atender
      </button>
    </div>
  );
}

function BadgeCorDeRisco({ cor }: { cor: "VERMELHO" | "AMARELO" | "VERDE" }) {
  const config = {
    VERMELHO: { texto: "Risco Alto",     bg: "bg-red-50",   ring: "ring-red-200",   text: "text-red-700"   },
    AMARELO:  { texto: "Risco Moderado", bg: "bg-amber-50", ring: "ring-amber-200", text: "text-amber-700" },
    VERDE:    { texto: "Risco Baixo",    bg: "bg-green-50", ring: "ring-green-200", text: "text-green-700" },
  }[cor];
  const ponto = { VERMELHO: "🔴", AMARELO: "🟡", VERDE: "🟢" }[cor];
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ${config.bg} ${config.ring} ${config.text}`}>
      {ponto} {config.texto}
    </span>
  );
}

const STATUSES_FINALIZAVEIS = new Set(["AGENDADA", "CONFIRMADA"]);

function AtendimentoLinha({
  atendimento,
  onFinalizar,
}: {
  atendimento: AtendimentoDoDiaDTO;
  onFinalizar: () => void;
}) {
  const statusConfig = {
    CONCLUIDO:      { texto: "Concluído",      cor: "#38A169" },
    EM_ATENDIMENTO: { texto: "Em Atendimento", cor: "#D69E2E" },
    AGUARDANDO:     { texto: "Aguardando",     cor: "#718096" },
  }[atendimento.status];
  const podeFinalizarr = STATUSES_FINALIZAVEIS.has(atendimento.statusRaw);
  return (
    <tr className="hover:bg-ink-50/40 transition-colors">
      <td className="whitespace-nowrap px-6 py-3 font-medium text-ink-800">
        {atendimento.horario}
      </td>
      <td className="px-6 py-3">
        <span className="font-medium text-ink-900">{atendimento.nomePet}</span>
        <br />
        <span className="text-xs text-ink-500">{atendimento.nomeTutor}</span>
      </td>
      <td className="whitespace-nowrap px-6 py-3">
        <span className="font-medium" style={{ color: statusConfig.cor }}>
          {statusConfig.texto}
        </span>
      </td>
      <td className="whitespace-nowrap px-6 py-3">
        {podeFinalizarr && (
          <button
            onClick={onFinalizar}
            className="rounded-lg border border-brand-300 bg-brand-50 px-3 py-1 text-xs font-medium text-brand-700 transition hover:bg-brand-100"
          >
            Finalizar consulta
          </button>
        )}
      </td>
    </tr>
  );
}

// ── Modal de finalização ───────────────────────────────────────────────────────

function ModalFinalizarConsulta({
  atendimento,
  service,
  onFechado,
  onFinalizado,
}: {
  atendimento: AtendimentoDoDiaDTO;
  service: MedicoService;
  onFechado: () => void;
  onFinalizado: () => void;
}) {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function finalizar(temRetorno: boolean, comExames: boolean) {
    setEnviando(true);
    setErro(null);
    try {
      await service.finalizarConsulta(atendimento.consultaId, temRetorno, comExames);
      onFinalizado();
    } catch (e) {
      setErro((e as Error).message ?? "Erro ao finalizar a consulta.");
      setEnviando(false);
    }
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onFechado(); }}
    >
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 className="text-lg font-bold text-ink-900">Finalizar Consulta</h2>
        <p className="mt-1 text-sm text-ink-600">
          <span className="font-medium">{atendimento.nomePet}</span> · {atendimento.nomeTutor}
        </p>
        <p className="mt-4 text-sm font-medium text-ink-800">
          Esta consulta dá direito a retorno?
        </p>

        {erro && (
          <div role="alert" className="mt-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-800">
            {erro}
          </div>
        )}

        <div className="mt-4 flex flex-col gap-3">
          <button
            disabled={enviando}
            onClick={() => finalizar(false, false)}
            className="w-full rounded-xl border border-ink-200 bg-ink-50 px-4 py-3 text-left text-sm transition hover:bg-ink-100 disabled:opacity-50"
          >
            <span className="block font-semibold text-ink-900">Não — encerrar sem retorno</span>
            <span className="text-xs text-ink-500">Consulta finalizada, sem agendamento de retorno.</span>
          </button>

          <button
            disabled={enviando}
            onClick={() => finalizar(true, false)}
            className="w-full rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-left text-sm transition hover:bg-brand-100 disabled:opacity-50"
          >
            <span className="block font-semibold text-brand-900">Sim — retorno simples</span>
            <span className="text-xs text-brand-700">Tutor poderá agendar retorno diretamente.</span>
          </button>

          <button
            disabled={enviando}
            onClick={() => finalizar(true, true)}
            className="w-full rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-left text-sm transition hover:bg-amber-100 disabled:opacity-50"
          >
            <span className="block font-semibold text-amber-900">Sim — retorno com exames pendentes</span>
            <span className="text-xs text-amber-700">Tutor precisará confirmar os exames antes de agendar o retorno.</span>
          </button>
        </div>

        <button
          onClick={onFechado}
          disabled={enviando}
          className="mt-4 w-full rounded-xl border border-ink-200 py-2 text-sm text-ink-600 transition hover:bg-ink-50 disabled:opacity-50"
        >
          Cancelar
        </button>
      </div>
    </div>
  );
}