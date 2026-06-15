import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  type AtendimentoDoDiaDTO,
  type ExameSolicitadoDTO,
  type FilaItemDTO,
  type MedicoService,
} from "./medicoService";

const INTERVALO_POLLING_MS = 15_000;

type Aba = "encaminhados" | "agendadas" | "retornos" | "finalizadas";

export function MedicoPainel() {
  const { apiFetch, session } = useAuth();
  const navigate = useNavigate();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  const [abaAtiva, setAbaAtiva] = useState<Aba>("agendadas");

  // ── Fila de espera ────────────────────────────────────────────────────────
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

  // Divisão por status para as abas
  const consultasAgendadas = atendimentos.filter(
    a => a.statusRaw === "AGENDADA" || a.statusRaw === "CONFIRMADA"
  );
  const consultasAguardandoRetorno = atendimentos.filter(
    a => a.statusRaw === "AGUARDANDO_RETORNO"
      || a.statusRaw === "EXAMES_SOLICITADOS"
      || a.statusRaw === "RETORNO_AGENDADO"
  );
  const consultasFinalizadas = atendimentos.filter(
    a => a.statusRaw === "REALIZADA"
  );

  // ── Modal de contexto de retorno ──────────────────────────────────────────
  const [consultaRetornoContexto, setConsultaRetornoContexto] =
    useState<AtendimentoDoDiaDTO | null>(null);

  const medicoNome = session?.user.nome ?? session?.user.identificador ?? "Médico";

  // Contagens para os badges nas abas
  const countEncaminhados = fila.length;
  const countAgendadas    = consultasAgendadas.length;
  const temPendenciaRetorno = consultasAguardandoRetorno.some(
    a => a.statusRaw === "AGUARDANDO_RETORNO" || a.statusRaw === "EXAMES_SOLICITADOS"
  );

  return (
    <div>
      <div className="mb-6">
        <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">
          Painel do Médico Veterinário
        </p>
        <h1 className="mt-1 text-2xl font-bold text-ink-900">{medicoNome}</h1>
      </div>

      {/* ── Card principal com abas ──────────────────────────────────────── */}
      <div className="card overflow-hidden p-0">

        {/* Cabeçalho das abas */}
        <div className="flex border-b border-ink-200 bg-ink-50/40">
          <BotaoAba
            ativo={abaAtiva === "encaminhados"}
            onClick={() => setAbaAtiva("encaminhados")}
            label="Encaminhados"
            count={countEncaminhados}
            alerta={false}
          />
          <BotaoAba
            ativo={abaAtiva === "agendadas"}
            onClick={() => setAbaAtiva("agendadas")}
            label="Consultas Agendadas"
            count={countAgendadas}
            alerta={false}
          />
          <BotaoAba
            ativo={abaAtiva === "retornos"}
            onClick={() => setAbaAtiva("retornos")}
            label="Aguardando Retorno"
            count={consultasAguardandoRetorno.length}
            alerta={temPendenciaRetorno}
          />
          <BotaoAba
            ativo={abaAtiva === "finalizadas"}
            onClick={() => setAbaAtiva("finalizadas")}
            label="Finalizadas"
            count={consultasFinalizadas.length}
            alerta={false}
          />
        </div>

        {/* ── Aba 1: Pacientes Encaminhados ─────────────────────────────── */}
        {abaAtiva === "encaminhados" && (
          <div>
            <div className="border-b border-ink-100 bg-white px-6 py-3">
              <p className="text-xs text-ink-500">
                Encaminhados pela recepção · atualizado a cada 15 s
              </p>
            </div>
            <div className="divide-y divide-ink-100">
              {carregandoFila ? (
                <div className="space-y-3 p-6">
                  {[0, 1, 2].map(i => (
                    <div key={i} className="h-16 animate-pulse rounded-xl bg-ink-100" />
                  ))}
                </div>
              ) : erroFila ? (
                <div className="p-6">
                  <div role="alert" className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
                    {erroFila}
                  </div>
                </div>
              ) : fila.length === 0 ? (
                <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
                  <span className="mb-2 text-3xl">🐾</span>
                  <p className="text-sm text-ink-500">Nenhum paciente encaminhado no momento.</p>
                </div>
              ) : (
                fila.map(item => (
                  <FilaItem
                    key={item.triagemId}
                    item={item}
                    onAtender={() => navigate(`/medico/prontuario/${item.pacienteId}`)}
                  />
                ))
              )}
            </div>
          </div>
        )}

        {/* ── Aba 2: Consultas Agendadas ────────────────────────────────── */}
        {abaAtiva === "agendadas" && (
          <div className="overflow-x-auto">
            {carregandoAtend ? (
              <div className="space-y-3 p-6">
                {[0, 1, 2].map(i => (
                  <div key={i} className="h-12 animate-pulse rounded-xl bg-ink-100" />
                ))}
              </div>
            ) : consultasAgendadas.length === 0 ? (
              <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
                <span className="mb-2 text-3xl">📋</span>
                <p className="text-sm text-ink-500">Nenhuma consulta agendada.</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-ink-100 bg-ink-50/50">
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Horário</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Paciente</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Status</th>
                    <th className="px-6 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100">
                  {consultasAgendadas.map((a, i) => (
                    <AtendimentoLinha
                      key={i}
                      atendimento={a}
                      onAtender={() => {
                        if (a.tipo === "RETORNO") {
                          setConsultaRetornoContexto(a);
                        } else {
                          navigate(`/medico/prontuario/${a.pacienteId}`, {
                            state: { consultaId: a.consultaId, tipoConsulta: "INICIAL" },
                          });
                        }
                      }}
                    />
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ── Aba 3: Aguardando Retorno ─────────────────────────────────── */}
        {abaAtiva === "retornos" && (
          <div className="overflow-x-auto">
            {carregandoAtend ? (
              <div className="space-y-3 p-6">
                {[0, 1, 2].map(i => (
                  <div key={i} className="h-12 animate-pulse rounded-xl bg-ink-100" />
                ))}
              </div>
            ) : consultasAguardandoRetorno.length === 0 ? (
              <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
                <span className="mb-2 text-3xl">🔄</span>
                <p className="text-sm text-ink-500">Nenhuma consulta aguardando retorno.</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-ink-100 bg-ink-50/50">
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Horário</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Paciente</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100">
                  {consultasAguardandoRetorno.map((a, i) => (
                    <FinalizadaLinha key={i} atendimento={a} />
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ── Aba 4: Finalizadas ────────────────────────────────────────── */}
        {abaAtiva === "finalizadas" && (
          <div className="overflow-x-auto">
            {carregandoAtend ? (
              <div className="space-y-3 p-6">
                {[0, 1, 2].map(i => (
                  <div key={i} className="h-12 animate-pulse rounded-xl bg-ink-100" />
                ))}
              </div>
            ) : consultasFinalizadas.length === 0 ? (
              <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
                <span className="mb-2 text-3xl">✅</span>
                <p className="text-sm text-ink-500">Nenhuma consulta finalizada.</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-ink-100 bg-ink-50/50">
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Horário</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Paciente</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100">
                  {consultasFinalizadas.map((a, i) => (
                    <FinalizadaLinha key={i} atendimento={a} />
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {consultaRetornoContexto && (
        <ModalContextoRetorno
          atendimento={consultaRetornoContexto}
          service={service}
          onFechado={() => setConsultaRetornoContexto(null)}
          onIrAoProntuario={() => {
            const ctx = consultaRetornoContexto;
            setConsultaRetornoContexto(null);
            navigate(`/medico/prontuario/${ctx.pacienteId}`, {
              state: { consultaId: ctx.consultaId, tipoConsulta: "RETORNO" },
            });
          }}
        />
      )}
    </div>
  );
}

// ── Subcomponentes ────────────────────────────────────────────────────────────

function BotaoAba({
  ativo, onClick, label, count, alerta,
}: {
  ativo: boolean;
  onClick: () => void;
  label: string;
  count: number;
  alerta: boolean;
}) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-2 border-b-2 px-6 py-4 text-sm font-medium transition ${
        ativo
          ? "border-brand-600 text-brand-700"
          : "border-transparent text-ink-500 hover:text-ink-700"
      }`}
    >
      {label}
      {count > 0 && (
        <span
          className={`inline-flex min-w-[1.25rem] items-center justify-center rounded-full px-1.5 py-0.5 text-[10px] font-bold ${
            alerta
              ? "bg-amber-100 text-amber-800"
              : ativo
              ? "bg-brand-100 text-brand-700"
              : "bg-ink-100 text-ink-600"
          }`}
        >
          {count}
        </span>
      )}
    </button>
  );
}

function FilaItem({ item, onAtender }: { item: FilaItemDTO; onAtender: () => void }) {
  return (
    <div className="flex items-center justify-between gap-4 px-6 py-4">
      <div className="min-w-0">
        <p className="truncate font-semibold text-ink-900">
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
      <button onClick={onAtender} className="btn-primary w-auto shrink-0 px-4 py-2 text-sm">
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

function AtendimentoLinha({
  atendimento,
  onAtender,
}: {
  atendimento: AtendimentoDoDiaDTO;
  onAtender: () => void;
}) {
  const statusLabel = atendimento.statusRaw === "CONFIRMADA" ? "Confirmada" : "Aguardando";
  const statusCor   = atendimento.statusRaw === "CONFIRMADA" ? "#38A169" : "#718096";
  return (
    <tr className="transition-colors hover:bg-ink-50/40">
      <td className="whitespace-nowrap px-6 py-3 font-medium text-ink-800">{atendimento.horario}</td>
      <td className="px-6 py-3">
        <div className="flex items-center gap-2">
          <span className="font-medium text-ink-900">{atendimento.nomePet}</span>
          {atendimento.tipo === "RETORNO" && (
            <span className="inline-flex items-center rounded-full bg-purple-50 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-purple-700 ring-1 ring-purple-200">
              Retorno
            </span>
          )}
        </div>
        <span className="text-xs text-ink-500">{atendimento.nomeTutor}</span>
      </td>
      <td className="whitespace-nowrap px-6 py-3">
        <span className="font-medium" style={{ color: statusCor }}>{statusLabel}</span>
      </td>
      <td className="whitespace-nowrap px-6 py-3">
        <button
          onClick={onAtender}
          className="btn-primary w-auto shrink-0 px-3 py-1.5 text-xs"
        >
          Atender
        </button>
      </td>
    </tr>
  );
}

const STATUS_FINALIZADA_CONFIG: Record<string, { texto: string; cor: string }> = {
  REALIZADA:          { texto: "Concluída",          cor: "#38A169" },
  AGUARDANDO_RETORNO: { texto: "Aguardando retorno", cor: "#C05621" },
  EXAMES_SOLICITADOS: { texto: "Exames solicitados", cor: "#B7791F" },
  RETORNO_AGENDADO:   { texto: "Retorno agendado",   cor: "#3182CE" },
};

function FinalizadaLinha({ atendimento }: { atendimento: AtendimentoDoDiaDTO }) {
  const cfg = STATUS_FINALIZADA_CONFIG[atendimento.statusRaw] ?? { texto: atendimento.statusRaw, cor: "#718096" };
  return (
    <tr className="transition-colors hover:bg-ink-50/40">
      <td className="whitespace-nowrap px-6 py-3 font-medium text-ink-800">{atendimento.horario}</td>
      <td className="px-6 py-3">
        <div className="flex items-center gap-2">
          <span className="font-medium text-ink-900">{atendimento.nomePet}</span>
          {atendimento.tipo === "RETORNO" && (
            <span className="inline-flex items-center rounded-full bg-purple-50 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-purple-700 ring-1 ring-purple-200">
              Retorno
            </span>
          )}
        </div>
        <span className="text-xs text-ink-500">{atendimento.nomeTutor}</span>
      </td>
      <td className="whitespace-nowrap px-6 py-3">
        <span className="font-medium" style={{ color: cfg.cor }}>{cfg.texto}</span>
      </td>
    </tr>
  );
}

// ── Modal de contexto de retorno ──────────────────────────────────────────────

function ModalContextoRetorno({
  atendimento,
  service,
  onFechado,
  onIrAoProntuario,
}: {
  atendimento: AtendimentoDoDiaDTO;
  service: MedicoService;
  onFechado: () => void;
  onIrAoProntuario: () => void;
}) {
  const [exames, setExames] = useState<ExameSolicitadoDTO[]>([]);
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    if (!atendimento.consultaOrigemId) return;
    setCarregando(true);
    service
      .buscarExamesDaOrigem(atendimento.consultaOrigemId)
      .then(setExames)
      .catch(() => setExames([]))
      .finally(() => setCarregando(false));
  }, [atendimento.consultaOrigemId, service]);

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={e => { if (e.target === e.currentTarget) onFechado(); }}
    >
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl">
        <div className="rounded-t-2xl border-b border-purple-100 bg-purple-50 px-6 py-4">
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center rounded-full bg-purple-100 px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wide text-purple-700 ring-1 ring-purple-200">
              Retorno
            </span>
            <h2 className="text-base font-bold text-ink-900">Consulta de Retorno</h2>
          </div>
          <p className="mt-1 text-sm text-ink-600">
            <span className="font-medium">{atendimento.nomePet}</span> · {atendimento.nomeTutor}
          </p>
        </div>

        <div className="space-y-4 px-6 py-4">
          <div>
            <p className="mb-1 text-xs font-semibold uppercase tracking-wider text-ink-400">
              Contexto da consulta de origem
            </p>
            {atendimento.consultaOrigemId ? (
              <p className="text-sm text-ink-600">
                ID da consulta original:{" "}
                <span className="font-mono text-xs text-ink-500">
                  {atendimento.consultaOrigemId.slice(0, 8)}…
                </span>
              </p>
            ) : (
              <p className="text-sm italic text-ink-400">Vínculo de origem não disponível.</p>
            )}
          </div>

          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-ink-400">
              Exames solicitados na consulta anterior
            </p>
            {carregando ? (
              <div className="space-y-2">
                {[0, 1, 2].map(i => (
                  <div key={i} className="h-8 animate-pulse rounded-lg bg-ink-100" />
                ))}
              </div>
            ) : exames.length === 0 ? (
              <p className="text-sm italic text-ink-400">Nenhum exame solicitado registrado.</p>
            ) : (
              <ul className="space-y-1.5">
                {exames.map(e => {
                  const concluido = e.status === "CONCLUIDO";
                  return (
                    <li
                      key={e.exameId}
                      className={`flex items-center justify-between rounded-lg border px-3 py-2 text-sm ${
                        concluido ? "border-green-200 bg-green-50" : "border-amber-200 bg-amber-50"
                      }`}
                    >
                      <span className={concluido ? "text-green-800" : "text-amber-800"}>{e.nome}</span>
                      <span className={`text-xs font-medium ${concluido ? "text-green-600" : "text-amber-600"}`}>
                        {concluido ? "✓ Concluído" : "Pendente"}
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        </div>

        <div className="flex gap-3 border-t border-ink-100 px-6 py-4">
          <button
            onClick={onFechado}
            className="flex-1 rounded-xl border border-ink-200 py-2.5 text-sm text-ink-600 transition hover:bg-ink-50"
          >
            Fechar
          </button>
          <button
            onClick={onIrAoProntuario}
            className="flex-1 btn-primary py-2.5 text-sm"
          >
            Ir ao prontuário →
          </button>
        </div>
      </div>
    </div>
  );
}

