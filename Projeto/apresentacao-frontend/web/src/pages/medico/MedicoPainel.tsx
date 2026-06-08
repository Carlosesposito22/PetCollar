import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  type AtendimentoDoDiaDTO,
  type FilaItemDTO,
} from "./medicoService";

const INTERVALO_POLLING_MS = 15_000;

export function MedicoPainel() {
  const { apiFetch, session } = useAuth();
  const navigate = useNavigate();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  // ── Fila de espera ─────────────────────────────────────────────────────────
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

  // Polling da fila a cada 15 s
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);
  useEffect(() => {
    void carregarFila();
    pollingRef.current = setInterval(() => { void carregarFila(); }, INTERVALO_POLLING_MS);
    return () => { if (pollingRef.current) clearInterval(pollingRef.current); };
  }, [carregarFila]);

  // ── Atendimentos do dia ────────────────────────────────────────────────────
  const [atendimentos, setAtendimentos] = useState<AtendimentoDoDiaDTO[]>([]);
  const [carregandoAtend, setCarregandoAtend] = useState(true);

  useEffect(() => {
    service.listarAtendimentosDoDia()
      .then(setAtendimentos)
      .finally(() => setCarregandoAtend(false));
  }, [service]);

  // ── Consultório do médico ─────────────────────────────────────────────────
  // TODO: buscar consultório vinculado ao médico via endpoint ainda não implementado.
  // Endpoint esperado: GET /api/medico/me  (retorna { consultorioId, consultorioNome, ... })
  // Funcionalidade relacionada: F-02 (Triagem) / F-10 (Relatório Clínico Evolutivo)
  const nomeConsultorio = "1";

  const medicoNome = session?.user.nome ?? session?.user.identificador ?? "Médico";

  return (
    <div>
      {/* Cabeçalho da página */}
      <div className="mb-6">
        <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">
          Painel do Médico Veterinário
        </p>
        <h1 className="mt-1 text-2xl font-bold text-ink-900">{medicoNome}</h1>
      </div>

      {/* Dois painéis lado a lado */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* ── Painel esquerdo: Pacientes Aguardando ───────────────────────── */}
        <section className="card flex flex-col p-0 overflow-hidden">
          <div className="border-b border-ink-200/60 px-6 py-4">
            <h2 className="text-base font-semibold text-ink-900">
              Pacientes Aguardando (Consultório {nomeConsultorio})
            </h2>
            <p className="mt-0.5 text-xs text-ink-500">
              Atualizado automaticamente a cada 15 s
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
                <p className="text-sm text-ink-500">Nenhum paciente aguardando no momento.</p>
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

        {/* ── Painel direito: Atendimentos do Dia ─────────────────────────── */}
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
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100">
                  {atendimentos.map((a, i) => (
                    <AtendimentoLinha key={i} atendimento={a} />
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>
      </div>

      {/* Nota informativa */}
      <div className="mt-8 rounded-3xl border border-dashed border-ink-300/70 bg-white/90 p-5 text-sm text-ink-600">
        Lista de Pacientes filtrada automaticamente por Vínculo de Consultório do médico.
        Atendimentos do dia atualizados em tempo real.
      </div>
    </div>
  );
}

// ── Subcomponentes ─────────────────────────────────────────────────────────────

function FilaItem({
  item,
  onAtender,
}: {
  item: FilaItemDTO;
  onAtender: () => void;
}) {
  // TODO: substituir pacienteId pelo nome real do pet e tutor quando
  // GET /api/medico/pacientes/:id estiver disponível (F-01 / F-10)
  const nomePet = `Paciente #${item.pacienteId.slice(0, 8)}`;
  const nomeTutor = "—";

  return (
    <div className="flex items-center justify-between gap-4 px-6 py-4">
      <div className="min-w-0">
        <p className="font-semibold text-ink-900 truncate">
          {nomePet}
          <span className="font-normal text-ink-500"> - {nomeTutor}</span>
        </p>
        <div className="mt-1">
          <BadgeCorDeRisco cor={item.corDeRisco} />
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
    VERMELHO: { texto: "Risco Alto",      bg: "bg-red-50",    ring: "ring-red-200",    text: "text-red-700"    },
    AMARELO:  { texto: "Risco Moderado",  bg: "bg-amber-50",  ring: "ring-amber-200",  text: "text-amber-700"  },
    VERDE:    { texto: "Risco Baixo",     bg: "bg-green-50",  ring: "ring-green-200",  text: "text-green-700"  },
  }[cor];

  const ponto = { VERMELHO: "🔴", AMARELO: "🟡", VERDE: "🟢" }[cor];

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ${config.bg} ${config.ring} ${config.text}`}
    >
      {ponto} {config.texto}
    </span>
  );
}

function AtendimentoLinha({ atendimento }: { atendimento: AtendimentoDoDiaDTO }) {
  const statusConfig = {
    CONCLUIDO:       { texto: "Concluído",       cor: "#38A169" },
    EM_ATENDIMENTO:  { texto: "Em Atendimento",  cor: "#D69E2E" },
    AGUARDANDO:      { texto: "Aguardando",      cor: "#718096" },
  }[atendimento.status];

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
    </tr>
  );
}
