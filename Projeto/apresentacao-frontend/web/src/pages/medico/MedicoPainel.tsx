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

  // ── Modal de contexto de retorno (ao clicar em Atender numa consulta de retorno) ──
  const [consultaRetornoContexto, setConsultaRetornoContexto] =
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
                      onAtender={() => {
                        if (a.tipo === "RETORNO") {
                          setConsultaRetornoContexto(a);
                        } else {
                          navigate(`/medico/prontuario/${a.pacienteId}`);
                        }
                      }}
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
  onAtender,
}: {
  atendimento: AtendimentoDoDiaDTO;
  onAtender: () => void;
}) {
  const statusConfig = {
    CONCLUIDO:          { texto: "Concluído",        cor: "#38A169" },
    EM_ATENDIMENTO:     { texto: "Em Atendimento",   cor: "#D69E2E" },
    AGUARDANDO:         { texto: "Aguardando",       cor: "#718096" },
    AGUARDANDO_RETORNO: { texto: "Aguardando retorno", cor: "#C05621" },
  }[atendimento.status];
  const podeAtender = STATUSES_FINALIZAVEIS.has(atendimento.statusRaw);
  return (
    <tr className="hover:bg-ink-50/40 transition-colors">
      <td className="whitespace-nowrap px-6 py-3 font-medium text-ink-800">
        {atendimento.horario}
      </td>
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
        <span className="font-medium" style={{ color: statusConfig.cor }}>
          {statusConfig.texto}
        </span>
      </td>
      <td className="whitespace-nowrap px-6 py-3">
        {podeAtender && (
          <button
            onClick={onAtender}
            className="btn-primary w-auto shrink-0 px-3 py-1.5 text-xs"
          >
            Atender
          </button>
        )}
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
      onClick={(e) => { if (e.target === e.currentTarget) onFechado(); }}
    >
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl">
        {/* Cabeçalho */}
        <div className="rounded-t-2xl bg-purple-50 px-6 py-4 border-b border-purple-100">
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

        {/* Corpo */}
        <div className="px-6 py-4 space-y-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-ink-400 mb-1">
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
              <p className="text-sm text-ink-400 italic">Vínculo de origem não disponível.</p>
            )}
          </div>

          {/* Exames solicitados */}
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-ink-400 mb-2">
              Exames solicitados na consulta anterior
            </p>
            {carregando ? (
              <div className="space-y-2">
                {[0, 1, 2].map((i) => (
                  <div key={i} className="h-8 animate-pulse rounded-lg bg-ink-100" />
                ))}
              </div>
            ) : exames.length === 0 ? (
              <p className="text-sm text-ink-400 italic">
                Nenhum exame solicitado registrado.
              </p>
            ) : (
              <ul className="space-y-1.5">
                {exames.map((e) => {
                  const concluido = e.status === "CONCLUIDO";
                  return (
                    <li
                      key={e.exameId}
                      className={`flex items-center justify-between rounded-lg border px-3 py-2 text-sm ${
                        concluido
                          ? "border-green-200 bg-green-50"
                          : "border-amber-200 bg-amber-50"
                      }`}
                    >
                      <span className={concluido ? "text-green-800" : "text-amber-800"}>
                        {e.nome}
                      </span>
                      <span
                        className={`text-xs font-medium ${
                          concluido ? "text-green-600" : "text-amber-600"
                        }`}
                      >
                        {concluido ? "✓ Concluído" : "Pendente"}
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        </div>

        {/* Rodapé */}
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

// ── Modal de finalização ───────────────────────────────────────────────────────

const EXAMES_PREDEFINIDOS = [
  "Hemograma completo",
  "Bioquímico sérico",
  "Urinálise (EAS)",
  "Radiografia",
  "Ultrassonografia abdominal",
  "Cultura e antibiograma",
  "PCR / Teste sorológico",
  "Exame parasitológico de fezes",
];

type EtapaModal = "retorno" | "tipo" | "exames";

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
  const [etapa, setEtapa] = useState<EtapaModal>("retorno");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [examesSelecionados, setExamesSelecionados] = useState<Set<string>>(new Set());
  const [examesExtras, setExamesExtras] = useState<string[]>([]);
  const [novoExame, setNovoExame] = useState("");

  function toggleExame(nome: string) {
    setExamesSelecionados(prev => {
      const next = new Set(prev);
      next.has(nome) ? next.delete(nome) : next.add(nome);
      return next;
    });
  }

  function adicionarExameExtra() {
    const trim = novoExame.trim();
    if (trim && !examesSelecionados.has(trim) && !examesExtras.includes(trim)) {
      setExamesExtras(prev => [...prev, trim]);
    }
    setNovoExame("");
  }

  function listaFinal() {
    return [...examesSelecionados, ...examesExtras];
  }

  async function finalizar(temRetorno: boolean, comExames: boolean, exames: string[]) {
    setEnviando(true);
    setErro(null);
    try {
      await service.finalizarConsulta(atendimento.consultaId, temRetorno, comExames, exames);
      onFinalizado();
    } catch (e) {
      setErro((e as Error).message ?? "Erro ao finalizar a consulta.");
      setEnviando(false);
    }
  }

  const isRetorno = atendimento.tipo === "RETORNO";

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(e) => { if (e.target === e.currentTarget && !enviando) onFechado(); }}
    >
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 className="text-lg font-bold text-ink-900">Finalizar Consulta</h2>
        <p className="mt-1 text-sm text-ink-600">
          <span className="font-medium">{atendimento.nomePet}</span> · {atendimento.nomeTutor}
        </p>

        {erro && (
          <div role="alert" className="mt-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-800">
            {erro}
          </div>
        )}

        {/* Consulta de retorno: apenas encerrar, sem nova elegibilidade de retorno */}
        {isRetorno && (
          <>
            <p className="mt-4 text-sm text-ink-600">
              Esta é uma consulta de <span className="font-semibold">retorno</span> — ao finalizar ela será encerrada sem gerar nova elegibilidade de retorno.
            </p>
            <button
              disabled={enviando}
              onClick={() => finalizar(false, false, [])}
              className="mt-4 w-full rounded-xl border border-brand-300 bg-brand-50 py-3 text-sm font-semibold text-brand-900 transition hover:bg-brand-100 disabled:opacity-50"
            >
              {enviando ? "Finalizando…" : "Finalizar consulta"}
            </button>
            <button onClick={onFechado} disabled={enviando}
              className="mt-3 w-full rounded-xl border border-ink-200 py-2 text-sm text-ink-600 transition hover:bg-ink-50 disabled:opacity-50">
              Cancelar
            </button>
          </>
        )}

        {/* Etapa 1: dá direito a retorno? (apenas para consultas iniciais) */}
        {!isRetorno && etapa === "retorno" && (
          <>
            <p className="mt-4 text-sm font-semibold text-ink-800">Esta consulta dá direito a retorno?</p>
            <div className="mt-3 flex flex-col gap-3">
              <button disabled={enviando} onClick={() => finalizar(false, false, [])}
                className="w-full rounded-xl border border-ink-200 bg-ink-50 px-4 py-3 text-left text-sm transition hover:bg-ink-100 disabled:opacity-50">
                <span className="block font-semibold text-ink-900">Não — encerrar sem retorno</span>
                <span className="text-xs text-ink-500">Consulta finalizada sem elegibilidade de retorno.</span>
              </button>
              <button disabled={enviando} onClick={() => setEtapa("tipo")}
                className="w-full rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-left text-sm transition hover:bg-brand-100 disabled:opacity-50">
                <span className="block font-semibold text-brand-900">Sim — liberar retorno</span>
                <span className="text-xs text-brand-700">O tutor poderá agendar o retorno pela plataforma.</span>
              </button>
            </div>
          </>
        )}

        {/* Etapa 2: tipo de retorno */}
        {!isRetorno && etapa === "tipo" && (
          <>
            <p className="mt-4 text-sm font-semibold text-ink-800">Tipo de retorno:</p>
            <div className="mt-3 flex flex-col gap-3">
              <button disabled={enviando} onClick={() => finalizar(true, false, [])}
                className="w-full rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-left text-sm transition hover:bg-brand-100 disabled:opacity-50">
                <span className="block font-semibold text-brand-900">Retorno simples</span>
                <span className="text-xs text-brand-700">Tutor agenda diretamente, sem etapa de exames.</span>
              </button>
              <button disabled={enviando} onClick={() => setEtapa("exames")}
                className="w-full rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-left text-sm transition hover:bg-amber-100 disabled:opacity-50">
                <span className="block font-semibold text-amber-900">Com exames pendentes</span>
                <span className="text-xs text-amber-700">Tutor precisa confirmar os exames antes de agendar.</span>
              </button>
            </div>
            <button onClick={() => setEtapa("retorno")} disabled={enviando}
              className="mt-4 text-xs text-ink-400 underline hover:text-ink-600 disabled:opacity-50">
              ← Voltar
            </button>
          </>
        )}

        {/* Etapa 3: selecionar exames */}
        {!isRetorno && etapa === "exames" && (
          <>
            <p className="mt-4 text-sm font-semibold text-ink-800">Exames solicitados para o retorno:</p>
            <ul className="mt-3 space-y-1.5 max-h-52 overflow-y-auto pr-1">
              {[...EXAMES_PREDEFINIDOS, ...examesExtras].map(nome => (
                <li key={nome}>
                  <label className="flex cursor-pointer items-center gap-3 rounded-lg border border-ink-200 px-3 py-2 text-sm transition hover:bg-ink-50 has-[:checked]:border-brand-300 has-[:checked]:bg-brand-50">
                    <input type="checkbox" className="accent-brand-600"
                      checked={examesSelecionados.has(nome) || examesExtras.includes(nome)}
                      onChange={() => {
                        if (examesExtras.includes(nome)) {
                          setExamesExtras(prev => prev.filter(e => e !== nome));
                        } else {
                          toggleExame(nome);
                        }
                      }} />
                    <span className="text-ink-800">{nome}</span>
                  </label>
                </li>
              ))}
            </ul>
            <div className="mt-3 flex gap-2">
              <input
                type="text"
                placeholder="Adicionar outro exame…"
                value={novoExame}
                onChange={e => setNovoExame(e.target.value)}
                onKeyDown={e => { if (e.key === "Enter") { e.preventDefault(); adicionarExameExtra(); }}}
                className="flex-1 rounded-lg border border-ink-300 px-3 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
              />
              <button type="button" onClick={adicionarExameExtra} disabled={!novoExame.trim()}
                className="rounded-lg border border-brand-300 bg-brand-50 px-3 py-1.5 text-sm font-medium text-brand-700 hover:bg-brand-100 disabled:opacity-40">
                + Adicionar
              </button>
            </div>
            <div className="mt-4 flex gap-2">
              <button onClick={() => setEtapa("tipo")} disabled={enviando}
                className="flex-1 rounded-xl border border-ink-200 py-2.5 text-sm text-ink-600 transition hover:bg-ink-50 disabled:opacity-50">
                ← Voltar
              </button>
              <button
                disabled={enviando || listaFinal().length === 0}
                onClick={() => finalizar(true, true, listaFinal())}
                className="flex-1 rounded-xl border border-amber-300 bg-amber-50 py-2.5 text-sm font-semibold text-amber-900 transition hover:bg-amber-100 disabled:opacity-50">
                {enviando ? "Finalizando…" : `Confirmar (${listaFinal().length} exame${listaFinal().length !== 1 ? "s" : ""})`}
              </button>
            </div>
          </>
        )}

        {!isRetorno && etapa !== "exames" && (
          <button onClick={onFechado} disabled={enviando}
            className="mt-4 w-full rounded-xl border border-ink-200 py-2 text-sm text-ink-600 transition hover:bg-ink-50 disabled:opacity-50">
            Cancelar
          </button>
        )}
      </div>
    </div>
  );
}