import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import type { Paciente } from "./TutorInicio";

// ── pdfjs-dist setup ─────────────────────────────────────────────────────────
// Worker carregado de forma lazy apenas quando o usuário abre a aba PDF
let pdfjsLib: typeof import("pdfjs-dist") | null = null;
async function carregarPdfjs() {
  if (pdfjsLib) return pdfjsLib;
  const mod = await import("pdfjs-dist");
  mod.GlobalWorkerOptions.workerSrc = new URL(
    "pdfjs-dist/build/pdf.worker.min.mjs",
    import.meta.url,
  ).href;
  pdfjsLib = mod;
  return mod;
}

// ── Tipos ────────────────────────────────────────────────────────────────────

type Aba = "carteira" | "calendario" | "pdf";
type StatusVacina = "APLICADA" | "PENDENTE" | "EM_ATRASO";
type StatusEvento = StatusVacina | "PREVISTA";
type TipoProtocolo = "FILHOTE" | "REFORCO_ANUAL" | "VIAGEM" | "PERSONALIZADO";
type FiltroStatus = "TODOS" | StatusVacina;

type Dose = {
  id: string;
  ciclo: string;
  rotulo: string;
  doseNumero: number;
  totalDoses: number;
  status: StatusVacina;
  data: string | null;
  medico: string | null;
  lote: string | null;
};

type Ciclo = {
  id: string;
  ciclo: string;
  totalDoses: number;
  aplicadas: number;
  registradas: number;
  podeAgendarProxima: boolean;
  tipoProtocolo: TipoProtocolo;
  intervaloDias: number | null;
  dataProximaDoseSugerida: string | null;
  diasLembrete: number | null;
  lembreteAtivo: boolean;
  doses: Dose[];
};

type Carteira = {
  pacienteNome: string;
  especie: string;
  raca: string;
  ciclos: Ciclo[];
};

type EventoVacinal = {
  data: string;
  pacienteId: string;
  pacienteNome: string;
  nomeCiclo: string;
  cicloId: string;
  doseId: string | null;
  doseNumero: number;
  totalDoses: number;
  status: StatusEvento;
  lembreteAtivo: boolean;
};

type VacinaExtraida = {
  id: string;
  nomeSugerido: string;
  dataAplicada: string | null;
  dataProxima: string | null;
  incluir: boolean;
};

// ── Constantes visuais ───────────────────────────────────────────────────────

const STATUS_VISUAL: Record<StatusVacina, { borda: string; fundo: string; badge: string; texto: string; label: string; icone: string }> = {
  APLICADA:  { borda: "border-l-emerald-500", fundo: "bg-emerald-50/40",   badge: "bg-emerald-100 text-emerald-700 ring-1 ring-emerald-200", texto: "text-emerald-700", label: "Aplicada",  icone: "✓"  },
  PENDENTE:  { borda: "border-l-amber-400",   fundo: "bg-amber-50/40",     badge: "bg-amber-100 text-amber-800 ring-1 ring-amber-200",       texto: "text-amber-700",   label: "Pendente",  icone: "◷"  },
  EM_ATRASO: { borda: "border-l-rose-500",    fundo: "bg-rose-50/40",      badge: "bg-rose-100 text-rose-700 ring-1 ring-rose-200",          texto: "text-rose-700",    label: "Em Atraso", icone: "⚠"  },
};

// No calendário, cada PET tem sua própria cor (estilo agenda). O status vira um
// pequeno glifo no evento — a cor identifica o pet, não o status.
const PALETA_PET = [
  { bar: "bg-sky-500",     dot: "bg-sky-500"     },
  { bar: "bg-violet-500",  dot: "bg-violet-500"  },
  { bar: "bg-emerald-600", dot: "bg-emerald-600" },
  { bar: "bg-amber-500",   dot: "bg-amber-500"   },
  { bar: "bg-rose-500",    dot: "bg-rose-500"    },
  { bar: "bg-teal-500",    dot: "bg-teal-500"    },
  { bar: "bg-indigo-500",  dot: "bg-indigo-500"  },
  { bar: "bg-fuchsia-500", dot: "bg-fuchsia-500" },
];

const STATUS_GLIFO: Record<StatusEvento, string> = {
  APLICADA: "✓", PENDENTE: "◷", EM_ATRASO: "⚠", PREVISTA: "◔",
};
const STATUS_LABEL: Record<StatusEvento, string> = {
  APLICADA: "Aplicada", PENDENTE: "Pendente", EM_ATRASO: "Em Atraso", PREVISTA: "Prevista",
};

/** Cor estável do pet a partir da posição dele na lista de pacientes do tutor. */
function corDoPet(pacienteId: string, pacientes: Paciente[]) {
  const idx = pacientes.findIndex(p => p.id === pacienteId);
  return PALETA_PET[(idx >= 0 ? idx : 0) % PALETA_PET.length];
}

const PROTOCOLO_INFO: Record<TipoProtocolo, { label: string; cor: string; dias: string }> = {
  FILHOTE:       { label: "Ciclo de Filhote",   cor: "bg-violet-100 text-violet-700 ring-1 ring-violet-200", dias: "21 dias"   },
  REFORCO_ANUAL: { label: "Reforço Anual",       cor: "bg-sky-100 text-sky-700 ring-1 ring-sky-200",         dias: "12 meses"  },
  VIAGEM:        { label: "Protocolo de Viagem", cor: "bg-teal-100 text-teal-700 ring-1 ring-teal-200",      dias: "30 dias"   },
  PERSONALIZADO: { label: "Personalizado",        cor: "bg-slate-100 text-slate-600 ring-1 ring-slate-200",   dias: "variável" },
};

const DIAS_SEMANA = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];
const MESES_PT    = ["Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"];

// ── Componente principal ─────────────────────────────────────────────────────

export function TutorVacinacao() {
  const { apiFetch } = useAuth();
  const [aba, setAba]                         = useState<Aba>("carteira");
  const [pacientes, setPacientes]             = useState<Paciente[]>([]);
  const [pacienteId, setPacienteId]           = useState("");
  const [carteira, setCarteira]               = useState<Carteira | null>(null);
  const [erro, setErro]                       = useState<string | null>(null);
  const [carregando, setCarregando]           = useState(false);
  const [filtro, setFiltro]                   = useState<FiltroStatus>("TODOS");
  const [soCompletos, setSoCompletos]         = useState(false);
  const [agendarProximaDe, setAgendarProximaDe] = useState<Ciclo | null>(null);
  const [agendarNova, setAgendarNova]         = useState(false);
  // Calendário
  const [calMes, setCalMes]                   = useState(() => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}`; });
  const [calEventos, setCalEventos]           = useState<EventoVacinal[]>([]);
  const [calCarregando, setCalCarregando]     = useState(false);
  const [diaSelecionado, setDiaSelecionado]   = useState<string | null>(null);

  useEffect(() => {
    apiFetch("/api/tutor/pacientes")
      .then(r => r.json())
      .then((ps: Paciente[]) => { setPacientes(ps); if (ps.length > 0) setPacienteId(ps[0].id); })
      .catch(() => setErro("Falha ao carregar pacientes."));
  }, [apiFetch]);

  const carregarCarteira = useCallback(async () => {
    if (!pacienteId) return;
    setCarregando(true); setErro(null);
    try {
      const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas`);
      if (!res.ok) throw new Error(`Falha ao carregar carteira (HTTP ${res.status}).`);
      setCarteira(await res.json());
    } catch (e) { setErro((e as Error).message); }
    finally { setCarregando(false); }
  }, [apiFetch, pacienteId]);

  useEffect(() => { void carregarCarteira(); }, [carregarCarteira]);

  const carregarCalendario = useCallback(async () => {
    setCalCarregando(true);
    try {
      const res = await apiFetch(`/api/tutor/calendario-vacinal?mes=${calMes}`);
      if (!res.ok) return;
      const data = await res.json() as { eventos: EventoVacinal[] };
      setCalEventos(data.eventos ?? []);
    } catch { /* silencioso */ }
    finally { setCalCarregando(false); }
  }, [apiFetch, calMes]);

  useEffect(() => { if (aba === "calendario") void carregarCalendario(); }, [aba, carregarCalendario]);

  const ciclosFiltrados = useMemo(() => {
    let ciclos = carteira?.ciclos ?? [];
    if (soCompletos) ciclos = ciclos.filter(c => c.aplicadas >= c.totalDoses);
    if (filtro !== "TODOS") {
      ciclos = ciclos.map(c => ({ ...c, doses: c.doses.filter(d => d.status === filtro) })).filter(c => c.doses.length > 0);
    }
    return ciclos;
  }, [carteira, filtro, soCompletos]);

  const estatisticas = useMemo(() => {
    const todos = carteira?.ciclos.flatMap(c => c.doses) ?? [];
    return { total: todos.length, aplicadas: todos.filter(d => d.status === "APLICADA").length, pendentes: todos.filter(d => d.status === "PENDENTE").length, atrasadas: todos.filter(d => d.status === "EM_ATRASO").length };
  }, [carteira]);

  const temAtraso = estatisticas.atrasadas > 0;

  // ── Navegação do calendário ────────────────────────────────────────────────
  function mudarMes(delta: number) {
    const [a, m] = calMes.split("-").map(Number);
    const d = new Date(a, m - 1 + delta, 1);
    setCalMes(`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}`);
    setDiaSelecionado(null);
  }

  return (
    <div className="space-y-6">
      {/* Cabeçalho + Abas */}
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-ink-900">Carteira Digital de Vacinação</h1>
          <p className="mt-1 text-sm text-ink-500">Acompanhe o ciclo vacinal, visualize previsões e proteja a saúde do seu pet.</p>
        </div>
      </div>

      {/* Navegação das abas */}
      <div className="inline-flex rounded-xl bg-white p-1 ring-1 ring-ink-300/60 shadow-sm">
        {(["carteira", "calendario", "pdf"] as Aba[]).map(a => (
          <button key={a} onClick={() => setAba(a)}
            className={"rounded-lg px-4 py-2 text-sm font-medium transition " +
              (aba === a ? "bg-brand-500 text-white shadow" : "text-ink-700 hover:bg-ink-100")}>
            {{ carteira: "💉 Carteira", calendario: "📅 Calendário", pdf: "📄 Importar PDF" }[a]}
          </button>
        ))}
      </div>

      {/* ── Aba: Carteira ─────────────────────────────────────────────────── */}
      {aba === "carteira" && (
        <>
          {/* Seletor de paciente + ações da carteira (ligadas ao pet) */}
          <div className="card p-4">
            <div className="flex flex-wrap items-end gap-4">
              <div className="min-w-[240px] flex-1">
                <label className="label" htmlFor="paciente">Paciente</label>
                <select id="paciente" className="input mt-1" value={pacienteId} onChange={e => setPacienteId(e.target.value)}>
                  {pacientes.length === 0 && <option value="">Nenhum paciente cadastrado</option>}
                  {pacientes.map(p => <option key={p.id} value={p.id}>{p.nome} — {p.especie}, {p.raca}</option>)}
                </select>
              </div>
              {carteira && (
                <div className="flex flex-wrap gap-2">
                  <button onClick={() => setAgendarNova(true)} className="btn-ghost ring-1 ring-ink-300 text-sm">+ Nova vacina</button>
                  <button onClick={() => setAba("pdf")} className="btn-ghost ring-1 ring-brand-300 text-brand-700 text-sm">📄 Importar PDF</button>
                  <button onClick={() => gerarCertificadoPDF(carteira)} className="btn-primary w-auto text-sm">⬇ Certificado</button>
                </div>
              )}
            </div>
          </div>

          {/* Resumo visual da carteira (barra empilhada + legendas) */}
          {carteira && <ResumoCarteira estatisticas={estatisticas} />}

          {temAtraso && (
            <div className="flex items-start gap-3 rounded-xl border border-rose-200 bg-rose-50 p-4 shadow-sm">
              <span className="mt-0.5 text-xl">⚠️</span>
              <div>
                <p className="font-semibold text-rose-800">Atenção — vacina(s) em atraso</p>
                <p className="mt-0.5 text-sm text-rose-700">
                  {estatisticas.atrasadas} dose{estatisticas.atrasadas > 1 ? "s" : ""} passaram da data prevista. Agende o reforço o quanto antes para manter a imunização ativa.
                </p>
              </div>
            </div>
          )}
          {erro && <div role="alert" className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{erro}</div>}
          {carregando && <div className="space-y-4">{[0,1,2].map(i => <div key={i} className="card h-36 animate-pulse bg-white/60" />)}</div>}

          {carteira && !carregando && (
            <>
              <div className="flex flex-wrap items-center gap-3">
                <div className="inline-flex rounded-xl bg-white p-1 ring-1 ring-ink-300/60 shadow-sm">
                  {(["TODOS","APLICADA","PENDENTE","EM_ATRASO"] as FiltroStatus[]).map(s => (
                    <button key={s} onClick={() => setFiltro(s)}
                      className={"rounded-lg px-3 py-1.5 text-sm font-medium transition " +
                        (filtro === s ? "bg-brand-500 text-white shadow" : "text-ink-700 hover:bg-ink-100")}>
                      {rotuloFiltro(s)}
                    </button>
                  ))}
                </div>
                <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-ink-700">
                  <input type="checkbox" className="h-4 w-4 rounded border-ink-300 text-brand-500 focus:ring-brand-500"
                    checked={soCompletos} onChange={e => setSoCompletos(e.target.checked)} />
                  Apenas ciclos concluídos
                </label>
              </div>

              {ciclosFiltrados.length === 0 ? (
                <div className="card px-6 py-16 text-center">
                  <p className="text-3xl">💉</p>
                  <p className="mt-3 text-sm text-ink-500">Nenhuma vacina para o filtro selecionado.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {ciclosFiltrados.map(c => (
                    <CicloCard key={c.id} ciclo={c} pacienteId={pacienteId}
                      onAgendarProxima={() => setAgendarProximaDe(c)}
                      onAtualizado={() => { void carregarCarteira(); }} />
                  ))}
                </div>
              )}
            </>
          )}
        </>
      )}

      {/* ── Aba: Calendário ───────────────────────────────────────────────── */}
      {aba === "calendario" && (
        <div className="space-y-4">
          {/* Navegação de mês */}
          <div className="card p-4">
            <div className="flex items-center justify-between">
              <button onClick={() => mudarMes(-1)} className="btn-ghost ring-1 ring-ink-300 px-3 py-1.5 text-sm">◀</button>
              <h2 className="text-lg font-bold text-ink-900">
                {MESES_PT[Number(calMes.split("-")[1]) - 1]} {calMes.split("-")[0]}
              </h2>
              <button onClick={() => mudarMes(1)} className="btn-ghost ring-1 ring-ink-300 px-3 py-1.5 text-sm">▶</button>
            </div>
            {/* Legenda — uma cor por pet */}
            {(() => {
              const ids = Array.from(new Set(calEventos.map(e => e.pacienteId)));
              return (
                <div className="mt-3 space-y-2">
                  {ids.length > 0 && (
                    <div className="flex flex-wrap gap-x-4 gap-y-1.5 text-xs">
                      {ids.map(id => {
                        const nome = calEventos.find(e => e.pacienteId === id)?.pacienteNome ?? "Pet";
                        return (
                          <span key={id} className="flex items-center gap-1.5 font-medium text-ink-700">
                            <span className={`h-2.5 w-2.5 rounded-full ${corDoPet(id, pacientes).dot}`} />
                            {nome}
                          </span>
                        );
                      })}
                    </div>
                  )}
                  <div className="flex flex-wrap gap-x-4 gap-y-1 text-[11px] text-ink-400">
                    {(["APLICADA","PENDENTE","EM_ATRASO","PREVISTA"] as StatusEvento[]).map(s => (
                      <span key={s} className="flex items-center gap-1">
                        <span>{STATUS_GLIFO[s]}</span>{STATUS_LABEL[s]}
                      </span>
                    ))}
                  </div>
                </div>
              );
            })()}
          </div>

          {calCarregando && <div className="card h-64 animate-pulse bg-white/60" />}

          {!calCarregando && (
            <>
              <CalendarioGrid mes={calMes} eventos={calEventos} pacientes={pacientes}
                diaSelecionado={diaSelecionado} onSelecionarDia={setDiaSelecionado} />

              {diaSelecionado && (
                <PainelDia data={diaSelecionado} eventos={calEventos.filter(e => e.data === diaSelecionado)}
                  pacientes={pacientes} onFechar={() => setDiaSelecionado(null)} />
              )}

              {calEventos.length === 0 && (
                <div className="card px-6 py-12 text-center">
                  <p className="text-3xl">📅</p>
                  <p className="mt-3 text-sm text-ink-500">Nenhum evento vacinal neste mês.</p>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {/* ── Aba: Importar PDF ─────────────────────────────────────────────── */}
      {aba === "pdf" && (
        <ImportarPDFTab pacientes={pacientes} apiFetch={apiFetch}
          onImportado={() => { setAba("carteira"); void carregarCarteira(); }} />
      )}

      {/* Modais */}
      {agendarProximaDe && (
        <AgendarProximaModal ciclo={agendarProximaDe} pacienteId={pacienteId}
          onFechar={() => setAgendarProximaDe(null)}
          onAgendado={() => { setAgendarProximaDe(null); void carregarCarteira(); }} />
      )}
      {agendarNova && (
        <AgendarNovaModal pacienteId={pacienteId}
          onFechar={() => setAgendarNova(false)}
          onAgendado={() => { setAgendarNova(false); void carregarCarteira(); }} />
      )}
    </div>
  );
}

// ── Sub-componentes — Carteira ───────────────────────────────────────────────

function ResumoCarteira({ estatisticas }: {
  estatisticas: { total: number; aplicadas: number; pendentes: number; atrasadas: number };
}) {
  const { total, aplicadas, pendentes, atrasadas } = estatisticas;
  const pct = (n: number) => (total > 0 ? (n / total) * 100 : 0);

  const segmentos = [
    { label: "Aplicadas", valor: aplicadas, barra: "bg-emerald-500", ponto: "bg-emerald-500", texto: "text-emerald-700", anel: "ring-emerald-100", fundo: "bg-emerald-50/50" },
    { label: "Pendentes", valor: pendentes, barra: "bg-amber-400",   ponto: "bg-amber-400",   texto: "text-amber-700",   anel: "ring-amber-100",   fundo: "bg-amber-50/50" },
    { label: "Em Atraso", valor: atrasadas, barra: "bg-rose-500",    ponto: "bg-rose-500",    texto: "text-rose-700",    anel: "ring-rose-100",    fundo: "bg-rose-50/50" },
  ];

  return (
    <div className="card p-5">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-ink-700">Resumo da carteira</h3>
        <span className="text-xs text-ink-500">{total} dose{total !== 1 ? "s" : ""} no total</span>
      </div>

      {/* Barra empilhada proporcional */}
      <div className="flex h-5 w-full overflow-hidden rounded-full bg-ink-100 ring-1 ring-ink-200/60">
        {total === 0 ? (
          <div className="flex w-full items-center justify-center text-[11px] text-ink-400">
            Nenhuma vacina cadastrada ainda
          </div>
        ) : (
          segmentos.map((s, i) =>
            s.valor > 0 ? (
              <div
                key={i}
                className={`${s.barra} h-full transition-all duration-500`}
                style={{ width: `${pct(s.valor)}%` }}
                title={`${s.label}: ${s.valor} (${Math.round(pct(s.valor))}%)`}
              />
            ) : null
          )
        )}
      </div>

      {/* Legendas com contagem e percentual */}
      <div className="mt-4 grid grid-cols-3 gap-3">
        {segmentos.map((s, i) => (
          <div key={i} className={`rounded-xl ${s.fundo} p-3 ring-1 ${s.anel}`}>
            <div className="flex items-center gap-2">
              <span className={`h-2.5 w-2.5 rounded-full ${s.ponto}`} />
              <span className="text-xs font-medium text-ink-600">{s.label}</span>
            </div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className={`text-2xl font-bold ${s.texto}`}>{s.valor}</span>
              <span className="text-xs text-ink-400">{total > 0 ? `${Math.round(pct(s.valor))}%` : "0%"}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function CicloCard({ ciclo, pacienteId, onAgendarProxima, onAtualizado }: {
  ciclo: Ciclo; pacienteId: string; onAgendarProxima: () => void; onAtualizado: () => void;
}) {
  const { apiFetch } = useAuth();
  const [expandido, setExpandido]         = useState(true);
  const [salvandoLembrete, setSalvando]   = useState(false);
  const [confirmarExcluir, setConfirmar]  = useState(false);
  const [excluindo, setExcluindo]         = useState(false);
  const pct     = ciclo.totalDoses > 0 ? Math.round((ciclo.aplicadas / ciclo.totalDoses) * 100) : 0;
  const info    = PROTOCOLO_INFO[ciclo.tipoProtocolo] ?? PROTOCOLO_INFO["PERSONALIZADO"];
  const completo  = ciclo.aplicadas >= ciclo.totalDoses;
  const temAtraso = ciclo.doses.some(d => d.status === "EM_ATRASO");

  async function salvarLembrete(dias: number | null) {
    setSalvando(true);
    try {
      await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas/${ciclo.id}/lembrete`, {
        method: "PATCH",
        body: JSON.stringify({ diasLembrete: dias }),
      });
      onAtualizado();
    } catch { /* silencioso */ }
    finally { setSalvando(false); }
  }

  async function excluirCiclo() {
    setExcluindo(true);
    try {
      await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas/${ciclo.id}`, { method: "DELETE" });
      onAtualizado();
    } catch { /* silencioso */ }
    finally { setExcluindo(false); setConfirmar(false); }
  }

  return (
    <div className={`card overflow-hidden transition-shadow hover:shadow-md ${temAtraso ? "ring-2 ring-rose-200" : ""}`}>
      <button className="w-full border-b border-ink-200/50 bg-gradient-to-r from-white to-ink-50/30 p-5 text-left"
        onClick={() => setExpandido(v => !v)}>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="text-lg font-bold text-ink-900">{ciclo.ciclo}</h3>
            {completo && <span className="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 ring-1 ring-emerald-200">✓ Ciclo completo</span>}
            {temAtraso && <span className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-2.5 py-0.5 text-xs font-semibold text-rose-700 ring-1 ring-rose-200">⚠ Em atraso</span>}
            <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${info.cor}`}>{info.label}</span>
            {ciclo.lembreteAtivo && <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-700 ring-1 ring-amber-200">🔔 Lembrete ativo</span>}
          </div>
          <div className="flex items-center gap-3">
            {ciclo.podeAgendarProxima && (
              <button onClick={e => { e.stopPropagation(); onAgendarProxima(); }}
                className="btn-ghost ring-1 ring-brand-300 text-brand-700 hover:bg-brand-50 text-sm">
                + Próxima dose
              </button>
            )}
            {!confirmarExcluir ? (
              <button onClick={e => { e.stopPropagation(); setConfirmar(true); }}
                className="btn-ghost ring-1 ring-rose-200 text-rose-600 hover:bg-rose-50 text-sm">
                🗑 Excluir
              </button>
            ) : (
              <span className="flex items-center gap-1.5" onClick={e => e.stopPropagation()}>
                <span className="text-xs font-medium text-rose-700">Excluir ciclo?</span>
                <button onClick={() => setConfirmar(false)} disabled={excluindo}
                  className="rounded-lg bg-white px-2 py-0.5 text-xs ring-1 ring-ink-300 hover:bg-ink-50">
                  Não
                </button>
                <button onClick={() => { void excluirCiclo(); }} disabled={excluindo}
                  className="rounded-lg bg-rose-500 px-2 py-0.5 text-xs text-white hover:bg-rose-600 disabled:opacity-60">
                  {excluindo ? "…" : "Sim, excluir"}
                </button>
              </span>
            )}
            <span className="text-lg text-ink-400">{expandido ? "▲" : "▼"}</span>
          </div>
        </div>

        {/* Barra de progresso */}
        <div className="mt-4 flex items-center gap-3">
          <div className="h-2.5 flex-1 overflow-hidden rounded-full bg-ink-200/60">
            <div className={`h-full rounded-full transition-all duration-500 ${completo ? "bg-emerald-500" : "bg-brand-500"}`}
              style={{ width: `${pct}%` }} />
          </div>
          <span className={`shrink-0 text-sm font-semibold ${completo ? "text-emerald-700" : "text-ink-600"}`}>
            {ciclo.aplicadas} / {ciclo.totalDoses} doses
          </span>
        </div>

        {/* Data sugerida */}
        {ciclo.dataProximaDoseSugerida && ciclo.podeAgendarProxima && (
          <div className="mt-3 inline-flex items-center gap-2 rounded-lg bg-brand-50 px-3 py-1.5 text-xs text-brand-800 ring-1 ring-brand-100">
            <span>📅</span>
            <span>Próxima dose pelo protocolo <strong>{info.label}</strong>: <strong>{formatarData(ciclo.dataProximaDoseSugerida)}</strong></span>
          </div>
        )}
      </button>

      {/* Configuração de lembrete */}
      <div className="border-b border-ink-100/60 bg-ink-50/30 px-5 py-3" onClick={e => e.stopPropagation()}>
        <div className="flex flex-wrap items-center gap-3">
          <span className="text-xs font-medium text-ink-600">🔔 Lembrete de dose:</span>
          <div className="flex gap-1.5">
            {([null, 7, 15, 30] as (number | null)[]).map(dias => (
              <button key={String(dias)} disabled={salvandoLembrete}
                onClick={() => { void salvarLembrete(dias); }}
                className={
                  "rounded-lg px-3 py-1 text-xs font-medium transition " +
                  (ciclo.diasLembrete === dias
                    ? "bg-brand-500 text-white shadow-sm"
                    : "bg-white text-ink-700 ring-1 ring-ink-300 hover:bg-brand-50 hover:ring-brand-300")
                }>
                {dias === null ? "Sem lembrete" : `${dias} dias`}
              </button>
            ))}
          </div>
          {salvandoLembrete && <span className="text-xs text-ink-400">Salvando…</span>}
        </div>
      </div>

      {/* Lista de doses */}
      {expandido && (
        <ul className="divide-y divide-ink-100/60">
          {ciclo.doses.map(d => <DoseLinha key={d.id} dose={d} />)}
        </ul>
      )}
    </div>
  );
}

function DoseLinha({ dose }: { dose: Dose }) {
  const vis = STATUS_VISUAL[dose.status] ?? STATUS_VISUAL["PENDENTE"];
  return (
    <li className={`flex flex-wrap items-center gap-x-6 gap-y-2 border-l-4 ${vis.borda} ${vis.fundo} px-5 py-3.5`}>
      <div className="flex min-w-[160px] items-center gap-2.5">
        <span className={`flex h-7 w-7 items-center justify-center rounded-full text-sm font-bold ${vis.badge}`}>{vis.icone}</span>
        <div>
          <span className="text-sm font-semibold text-ink-900">{dose.totalDoses > 1 ? `Dose ${dose.doseNumero}/${dose.totalDoses}` : "Dose única"}</span>
          <span className={`ml-2 inline-flex rounded-full px-2 py-0.5 text-xs font-semibold ${vis.badge}`}>{vis.label}</span>
        </div>
      </div>
      {dose.data   && <InfoChip icone="📅" rotulo="Data"      valor={formatarData(dose.data)} />}
      {dose.medico && <InfoChip icone="👨‍⚕️" rotulo="Médico"    valor={dose.medico} />}
      {dose.lote   && <InfoChip icone="🔖" rotulo="Lote/Selo" valor={dose.lote} />}
    </li>
  );
}

function InfoChip({ icone, rotulo, valor }: { icone: string; rotulo: string; valor: string }) {
  return (
    <div className="flex items-center gap-1.5 text-sm">
      <span className="text-base">{icone}</span>
      <span className="font-medium text-ink-500">{rotulo}:</span>
      <span className="text-ink-800">{valor}</span>
    </div>
  );
}

// ── Sub-componentes — Calendário ─────────────────────────────────────────────

function CalendarioGrid({ mes, eventos, pacientes, diaSelecionado, onSelecionarDia }: {
  mes: string; eventos: EventoVacinal[]; pacientes: Paciente[];
  diaSelecionado: string | null; onSelecionarDia: (d: string) => void;
}) {
  const MAX_VISIVEL = 2; // chips mostrados por dia antes do "+N"
  const [ano, mesNum] = mes.split("-").map(Number);
  const primeiroDia   = new Date(ano, mesNum - 1, 1).getDay(); // 0=Dom
  const totalDias     = new Date(ano, mesNum, 0).getDate();
  const eventosPorDia = useMemo(() => {
    const m: Record<string, EventoVacinal[]> = {};
    eventos.forEach(e => { (m[e.data] ??= []).push(e); });
    return m;
  }, [eventos]);

  const celulas: (number | null)[] = [...Array(primeiroDia).fill(null), ...Array.from({ length: totalDias }, (_, i) => i + 1)];
  // Preenche até múltiplo de 7
  while (celulas.length % 7 !== 0) celulas.push(null);

  return (
    <div className="card overflow-hidden">
      {/* Cabeçalho dos dias */}
      <div className="grid grid-cols-7 border-b border-ink-200 bg-ink-50">
        {DIAS_SEMANA.map(d => <div key={d} className="py-2 text-center text-xs font-semibold text-ink-500">{d}</div>)}
      </div>
      {/* Grade de dias */}
      <div className="grid grid-cols-7">
        {celulas.map((dia, i) => {
          if (dia === null) return <div key={i} className="min-h-[104px] border-b border-r border-ink-100/60 bg-ink-50/30" />;
          const isoData  = `${mes}-${String(dia).padStart(2,"0")}`;
          const evsDia   = eventosPorDia[isoData] ?? [];
          const hoje     = new Date().toISOString().slice(0,10);
          const eSelecionado = diaSelecionado === isoData;
          const eHoje    = isoData === hoje;
          const visiveis = evsDia.slice(0, MAX_VISIVEL);
          const extra    = evsDia.length - visiveis.length;
          return (
            <button key={i} onClick={() => evsDia.length > 0 ? onSelecionarDia(isoData) : undefined}
              className={
                "min-h-[104px] border-b border-r border-ink-100/60 p-1.5 text-left align-top transition " +
                (eSelecionado ? "bg-brand-50 ring-2 ring-inset ring-brand-400 " : "hover:bg-ink-50 ") +
                (evsDia.length > 0 ? "cursor-pointer " : "cursor-default ")
              }>
              <span className={
                "inline-flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold " +
                (eHoje ? "bg-brand-500 text-white" : "text-ink-700")
              }>{dia}</span>
              {/* Eventos como chips coloridos por pet (estilo agenda) */}
              <div className="mt-1 space-y-0.5">
                {visiveis.map((ev, j) => (
                  <span key={j}
                    title={`${ev.pacienteNome} — ${ev.nomeCiclo} · ${STATUS_LABEL[ev.status]}`}
                    className={`block w-full truncate rounded px-1.5 py-0.5 text-[10px] font-medium leading-tight text-white ${corDoPet(ev.pacienteId, pacientes).bar}`}>
                    <span className="mr-0.5">{STATUS_GLIFO[ev.status]}</span>{ev.nomeCiclo}
                  </span>
                ))}
                {extra > 0 && (
                  <span className="block px-1 text-[10px] font-semibold text-ink-500">+{extra} mais</span>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}

function PainelDia({ data, eventos, pacientes, onFechar }: {
  data: string; eventos: EventoVacinal[]; pacientes: Paciente[]; onFechar: () => void;
}) {
  return (
    <div className="card p-5">
      <div className="mb-4 flex items-center justify-between">
        <h3 className="font-bold text-ink-900">📅 {formatarData(data)} — {eventos.length} evento{eventos.length !== 1 ? "s" : ""}</h3>
        <button onClick={onFechar} className="flex h-7 w-7 items-center justify-center rounded-full text-ink-400 hover:bg-ink-100">✕</button>
      </div>
      <div className="space-y-2">
        {eventos.map((ev, i) => (
          <div key={i} className="flex items-center gap-3 rounded-lg border border-ink-200 bg-white px-4 py-3">
            <span className={`h-3 w-3 shrink-0 rounded-full ${corDoPet(ev.pacienteId, pacientes).dot}`} title={ev.pacienteNome} />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-ink-900">{ev.pacienteNome} — {ev.nomeCiclo}</p>
              <p className="text-xs text-ink-500">
                Dose {ev.doseNumero}/{ev.totalDoses} · <span className="font-medium">{STATUS_GLIFO[ev.status]} {STATUS_LABEL[ev.status]}</span>
              </p>
            </div>
            {ev.lembreteAtivo && <span className="text-amber-500 text-sm" title="Lembrete ativo">🔔</span>}
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Sub-componentes — Importar PDF ───────────────────────────────────────────

function ImportarPDFTab({ pacientes, apiFetch, onImportado }: {
  pacientes: Paciente[];
  apiFetch: ReturnType<typeof useAuth>["apiFetch"];
  onImportado: () => void;
}) {
  const [etapa, setEtapa]               = useState<"upload" | "revisao" | "confirmando">("upload");
  const [vacinas, setVacinas]           = useState<VacinaExtraida[]>([]);
  const [pacienteId, setPacienteId]     = useState(pacientes[0]?.id ?? "");
  const [extraindo, setExtraindo]       = useState(false);
  const [erroExtracao, setErroExtracao] = useState<string | null>(null);
  const [enviando, setEnviando]         = useState(false);
  const [erroEnvio, setErroEnvio]       = useState<string | null>(null);
  const fileRef                         = useRef<HTMLInputElement>(null);

  async function processarArquivo(file: File) {
    setExtraindo(true); setErroExtracao(null);
    try {
      const pdfjs = await carregarPdfjs();
      const buffer = await file.arrayBuffer();
      const pdf = await pdfjs.getDocument({ data: new Uint8Array(buffer) }).promise;
      let texto = "";
      for (let i = 1; i <= pdf.numPages; i++) {
        const page = await pdf.getPage(i);
        const content = await page.getTextContent();
        const itens = (content.items as { str: string; transform: number[] }[]);
        itens.sort((a, b) => {
          const dy = b.transform[5] - a.transform[5];
          return Math.abs(dy) > 5 ? dy : a.transform[4] - b.transform[4];
        });
        texto += itens.map(it => it.str).join(" ") + "\n";
      }
      const extraidas = parsearVacinasDoPDF(texto);
      if (extraidas.length === 0) {
        setErroExtracao("Nenhuma vacina reconhecida no PDF. Verifique se o arquivo é uma carteira de vacinação veterinária.");
        return;
      }
      setVacinas(extraidas);
      setEtapa("revisao");
    } catch (e) {
      setErroExtracao("Falha ao processar o PDF: " + (e as Error).message);
    } finally {
      setExtraindo(false);
    }
  }

  async function confirmarImportacao() {
    const selecionadas = vacinas.filter(v => v.incluir && v.nomeSugerido.trim());
    if (selecionadas.length === 0) { setErroEnvio("Selecione ao menos uma vacina para importar."); return; }
    if (!pacienteId) { setErroEnvio("Selecione o paciente."); return; }
    setEnviando(true); setErroEnvio(null);
    let ok = 0;
    for (const v of selecionadas) {
      const dataBase = v.dataAplicada ?? new Date().toISOString().slice(0, 10);
      try {
        const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas`, {
          method: "POST",
          body: JSON.stringify({
            ciclo: v.nomeSugerido.trim(),
            totalDoses: 1,
            data: dataBase,
            tipoProtocolo: "PERSONALIZADO",
            intervaloDias: 365,
          }),
        });
        if (res.ok) ok++;
      } catch { /* segue */ }
    }
    setEnviando(false);
    if (ok > 0) {
      setEtapa("confirmando");
      setTimeout(onImportado, 2000);
    } else {
      setErroEnvio("Falha ao criar os ciclos vacinais. Tente novamente.");
    }
  }

  if (etapa === "confirmando") {
    return (
      <div className="card flex flex-col items-center gap-4 py-16 text-center">
        <span className="text-5xl">✅</span>
        <p className="text-lg font-bold text-emerald-700">Importação concluída!</p>
        <p className="text-sm text-ink-500">Redirecionando para a carteira…</p>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {etapa === "upload" && (
        <div className="card p-6 space-y-5">
          <div>
            <h2 className="text-lg font-bold text-ink-900">Importar Carteira de Vacinação (PDF)</h2>
            <p className="mt-1 text-sm text-ink-500">
              Faça upload de um PDF da carteira de vacinação do seu pet. O sistema extrai os nomes das vacinas e datas automaticamente — você revisa antes de confirmar.
            </p>
          </div>

          {/* Área de upload */}
          <label className={
            "flex cursor-pointer flex-col items-center gap-3 rounded-2xl border-2 border-dashed p-10 transition " +
            (extraindo ? "border-brand-300 bg-brand-50/30" : "border-ink-300 hover:border-brand-400 hover:bg-brand-50/20")
          }>
            <span className="text-4xl">{extraindo ? "⏳" : "📄"}</span>
            <div className="text-center">
              <p className="font-semibold text-ink-800">{extraindo ? "Extraindo vacinas…" : "Clique ou arraste o PDF aqui"}</p>
              <p className="mt-0.5 text-xs text-ink-400">Apenas arquivos .pdf · Máx. 10 MB</p>
            </div>
            <input ref={fileRef} type="file" accept="application/pdf" className="hidden"
              onChange={e => { const f = e.target.files?.[0]; if (f) void processarArquivo(f); }} />
          </label>

          {erroExtracao && (
            <div role="alert" className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{erroExtracao}</div>
          )}

          <div className="rounded-xl bg-sky-50 p-4 text-sm text-sky-800 ring-1 ring-sky-200">
            <p className="font-semibold">Como funciona a extração automática?</p>
            <ul className="mt-2 list-inside list-disc space-y-1 text-xs text-sky-700">
              <li>O PDF é processado localmente — nenhum dado é enviado a servidores externos.</li>
              <li>Vacinas reconhecidas: V10, V8, Antirrábica, Giardíase, Leptospirose e outras.</li>
              <li>Datas no formato DD/MM/AAAA são extraídas automaticamente.</li>
              <li>Você revisa e edita tudo antes de confirmar.</li>
            </ul>
          </div>
        </div>
      )}

      {etapa === "revisao" && (
        <div className="card p-6 space-y-5">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-bold text-ink-900">Revisar vacinas extraídas</h2>
              <p className="mt-0.5 text-sm text-ink-500">{vacinas.filter(v => v.incluir).length} de {vacinas.length} selecionadas para importar.</p>
            </div>
            <button onClick={() => { setEtapa("upload"); setVacinas([]); }}
              className="btn-ghost ring-1 ring-ink-300 text-sm">← Voltar</button>
          </div>

          {/* Seletor de paciente */}
          <div className="grid gap-1">
            <label className="label">Paciente de destino</label>
            <select className="input" value={pacienteId} onChange={e => setPacienteId(e.target.value)}>
              {pacientes.map(p => <option key={p.id} value={p.id}>{p.nome} — {p.especie}, {p.raca}</option>)}
            </select>
          </div>

          {/* Tabela de revisão */}
          <div className="overflow-x-auto rounded-xl border border-ink-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-ink-50 text-xs font-semibold text-ink-600 [&>th]:px-4 [&>th]:py-2.5 [&>th]:text-left">
                  <th className="w-8"><input type="checkbox"
                    checked={vacinas.every(v => v.incluir)}
                    onChange={e => setVacinas(vs => vs.map(v => ({ ...v, incluir: e.target.checked })))} /></th>
                  <th>Nome da Vacina</th>
                  <th>Data Aplicada</th>
                  <th>Próxima Dose</th>
                </tr>
              </thead>
              <tbody>
                {vacinas.map((v, i) => (
                  <tr key={v.id} className="border-t border-ink-100 [&>td]:px-4 [&>td]:py-2.5">
                    <td>
                      <input type="checkbox" checked={v.incluir}
                        onChange={e => setVacinas(vs => vs.map((x, j) => j === i ? { ...x, incluir: e.target.checked } : x))} />
                    </td>
                    <td>
                      <input className="input py-1 text-sm" value={v.nomeSugerido}
                        onChange={e => setVacinas(vs => vs.map((x, j) => j === i ? { ...x, nomeSugerido: e.target.value } : x))} />
                    </td>
                    <td>
                      <input type="date" className="input py-1 text-sm" value={v.dataAplicada ?? ""}
                        onChange={e => setVacinas(vs => vs.map((x, j) => j === i ? { ...x, dataAplicada: e.target.value || null } : x))} />
                    </td>
                    <td>
                      <input type="date" className="input py-1 text-sm" value={v.dataProxima ?? ""}
                        onChange={e => setVacinas(vs => vs.map((x, j) => j === i ? { ...x, dataProxima: e.target.value || null } : x))} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {erroEnvio && <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-900">{erroEnvio}</div>}

          <div className="flex justify-end gap-3">
            <button onClick={() => { setEtapa("upload"); setVacinas([]); }}
              className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
            <button onClick={() => { void confirmarImportacao(); }} disabled={enviando}
              className="btn-primary w-auto">
              {enviando ? "Importando…" : `Importar ${vacinas.filter(v => v.incluir).length} vacina(s)`}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Modais ───────────────────────────────────────────────────────────────────

function AgendarProximaModal({ ciclo, pacienteId, onFechar, onAgendado }: {
  ciclo: Ciclo; pacienteId: string; onFechar: () => void; onAgendado: () => void;
}) {
  const { apiFetch } = useAuth();
  const info    = PROTOCOLO_INFO[ciclo.tipoProtocolo] ?? PROTOCOLO_INFO["PERSONALIZADO"];
  const sugerida = ciclo.dataProximaDoseSugerida ? ciclo.dataProximaDoseSugerida.slice(0,10) : "";
  const [data, setData]           = useState(sugerida);
  const [erro, setErro]           = useState<string | null>(null);
  const [enviando, setEnviando]   = useState(false);
  const proxNum = ciclo.registradas + 1;

  async function confirmar(e: React.FormEvent) {
    e.preventDefault(); setErro(null); setEnviando(true);
    try {
      const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas/proxima-dose`, {
        method: "POST", body: JSON.stringify({ ciclo: ciclo.ciclo, data }),
      });
      if (!res.ok) { const b = await res.json().catch(() => ({} as { mensagem?: string })); throw new Error(b?.mensagem ?? `HTTP ${res.status}`); }
      onAgendado();
    } catch (e2) { setErro((e2 as Error).message); }
    finally { setEnviando(false); }
  }

  return (
    <ModalShell titulo={`Agendar Dose ${proxNum}/${ciclo.totalDoses} — ${ciclo.ciclo}`} onFechar={onFechar}>
      <div className="mb-5 rounded-xl bg-gradient-to-r from-brand-50 to-brand-50/40 p-4 ring-1 ring-brand-100">
        <div className="flex items-start gap-3">
          <span className="text-2xl">💉</span>
          <div>
            <p className="font-semibold text-brand-900">{ciclo.ciclo}</p>
            <p className="mt-0.5 text-sm text-brand-700">Protocolo: <span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${info.cor}`}>{info.label}</span></p>
            {ciclo.dataProximaDoseSugerida && (
              <p className="mt-1 text-sm text-brand-700">📅 Data sugerida: <strong>{formatarData(ciclo.dataProximaDoseSugerida)}</strong></p>
            )}
          </div>
        </div>
      </div>
      {erro && <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{erro}</div>}
      <form onSubmit={confirmar} className="grid gap-4">
        <div>
          <label className="label" htmlFor="dataProx">Data do agendamento</label>
          <input id="dataProx" type="date" required className="input mt-1"
            min={new Date().toISOString().slice(0,10)} value={data} onChange={e => setData(e.target.value)} />
          {ciclo.dataProximaDoseSugerida && <p className="mt-1 text-xs text-ink-500">Pré-preenchida com a previsão do protocolo {info.label} ({info.dias}).</p>}
        </div>
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
          <button type="submit" disabled={enviando} className="btn-primary w-auto">{enviando ? "Agendando…" : "Confirmar Agendamento"}</button>
        </div>
      </form>
    </ModalShell>
  );
}

const PROTOCOLOS_OPCOES: { value: TipoProtocolo; label: string; descricao: string }[] = [
  { value: "FILHOTE",       label: "Ciclo de Filhote",   descricao: "Doses a cada 21 dias — para V10, V8 em filhotes." },
  { value: "REFORCO_ANUAL", label: "Reforço Anual",       descricao: "Dose a cada 12 meses — Antirrábica, V10 adulto." },
  { value: "VIAGEM",        label: "Protocolo de Viagem", descricao: "Doses a cada 30 dias — viagens internacionais." },
  { value: "PERSONALIZADO", label: "Personalizado",        descricao: "Intervalo definido pelo veterinário." },
];

function AgendarNovaModal({ pacienteId, onFechar, onAgendado }: {
  pacienteId: string; onFechar: () => void; onAgendado: () => void;
}) {
  const { apiFetch } = useAuth();
  const [ciclo, setCiclo]               = useState("");
  const [totalDoses, setTotalDoses]     = useState(1);
  const [data, setData]                 = useState("");
  const [protocolo, setProtocolo]       = useState<TipoProtocolo>("FILHOTE");
  const [intervaloDias, setIntervaloDias] = useState(30);
  const [erro, setErro]                 = useState<string | null>(null);
  const [enviando, setEnviando]         = useState(false);

  async function confirmar(e: React.FormEvent) {
    e.preventDefault(); setErro(null); setEnviando(true);
    try {
      const body: Record<string, unknown> = { ciclo, totalDoses, data, tipoProtocolo: protocolo };
      if (protocolo === "PERSONALIZADO") body.intervaloDias = intervaloDias;
      const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas`, { method: "POST", body: JSON.stringify(body) });
      if (!res.ok) { const j = await res.json().catch(() => ({} as { mensagem?: string })); throw new Error(j?.mensagem ?? `HTTP ${res.status}`); }
      onAgendado();
    } catch (e2) { setErro((e2 as Error).message); }
    finally { setEnviando(false); }
  }

  return (
    <ModalShell titulo="Agendar Nova Vacina" onFechar={onFechar}>
      {erro && <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{erro}</div>}
      <form onSubmit={confirmar} className="grid gap-4">
        <div>
          <label className="label" htmlFor="nomeVacina">Nome da vacina</label>
          <input id="nomeVacina" required className="input mt-1" placeholder="Ex.: V10, Antirrábica, Giardíase"
            value={ciclo} onChange={e => setCiclo(e.target.value)} />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label" htmlFor="totalDoses">Nº de doses do ciclo</label>
            <input id="totalDoses" type="number" min={1} max={10} required className="input mt-1"
              value={totalDoses} onChange={e => setTotalDoses(Number(e.target.value))} />
          </div>
          <div>
            <label className="label" htmlFor="dataNova">Data da 1ª dose</label>
            <input id="dataNova" type="date" required className="input mt-1"
              min={new Date().toISOString().slice(0,10)} value={data} onChange={e => setData(e.target.value)} />
          </div>
        </div>
        <div>
          <label className="label">Protocolo vacinal</label>
          <div className="mt-1 grid gap-2 sm:grid-cols-2">
            {PROTOCOLOS_OPCOES.map(op => (
              <button key={op.value} type="button" onClick={() => setProtocolo(op.value)}
                className={"flex flex-col rounded-xl border p-3 text-left transition " +
                  (protocolo === op.value ? "border-brand-400 bg-brand-50 ring-2 ring-brand-300" : "border-ink-200 hover:border-brand-300 hover:bg-brand-50/30")}>
                <span className="text-sm font-semibold text-ink-900">{op.label}</span>
                <span className="mt-0.5 text-xs text-ink-500">{op.descricao}</span>
              </button>
            ))}
          </div>
        </div>
        {protocolo === "PERSONALIZADO" && (
          <div>
            <label className="label" htmlFor="intervaloDias">Intervalo entre doses (dias)</label>
            <input id="intervaloDias" type="number" min={1} max={730} required className="input mt-1"
              value={intervaloDias} onChange={e => setIntervaloDias(Number(e.target.value))} />
          </div>
        )}
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
          <button type="submit" disabled={enviando} className="btn-primary w-auto">{enviando ? "Agendando…" : "Criar Ciclo Vacinal"}</button>
        </div>
      </form>
    </ModalShell>
  );
}

function ModalShell({ titulo, children, onFechar }: { titulo: string; children: React.ReactNode; onFechar: () => void }) {
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 p-4 sm:items-center" onClick={onFechar}>
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl ring-1 ring-ink-200" onClick={e => e.stopPropagation()}>
        <div className="mb-5 flex items-center justify-between">
          <h3 className="text-lg font-bold text-ink-900">{titulo}</h3>
          <button onClick={onFechar} className="flex h-7 w-7 items-center justify-center rounded-full text-ink-400 hover:bg-ink-100 hover:text-ink-700">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

// ── Extração de PDF ──────────────────────────────────────────────────────────

const VACINAS_CONHECIDAS = [
  "QUÁDRUPLA FELINA","TRÍPLICE FELINA","QUÁDRUPLA","TRÍPLICE",
  "V10","V8","V5","V4",
  "ANTIRRÁBICA","ANTIRRABICA","ANTIRRÁBICO","ANTIRRABICO",
  "TETRAVALENTE","PENTAVALENTE","HEXAVALENTE","POLIVALENTE",
  "GIARDÍASE","GIARDIASE","LEPTOSPIROSE","BORDETELLA",
  "CORONAVÍRUS","CORONAVIRUS","RAIVA","INFLUENZA","CINOMOSE","PARVOVIROSE",
];

function parsearVacinasDoPDF(texto: string): VacinaExtraida[] {
  const textoUpper = texto.toUpperCase();
  const DATA_RE = /(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{2,4})/g;

  function extrairDatas(trecho: string): string[] {
    const re = new RegExp(DATA_RE.source, "g");
    const datas: string[] = [];
    let m: RegExpExecArray | null;
    while ((m = re.exec(trecho)) !== null) {
      const d = m[1].padStart(2, "0"), mes = m[2].padStart(2, "0");
      const ano = m[3].length === 2 ? "20" + m[3] : m[3];
      const iso = `${ano}-${mes}-${d}`;
      const dt = new Date(iso);
      if (!isNaN(dt.getTime()) && dt.getFullYear() >= 2018) datas.push(iso);
    }
    return datas;
  }

  const encontradas: VacinaExtraida[] = [];
  const vistasChave = new Set<string>();

  for (const nome of VACINAS_CONHECIDAS) {
    const idx = textoUpper.indexOf(nome);
    if (idx === -1) continue;
    const chave = nome.toLowerCase();
    if (vistasChave.has(chave)) continue;
    vistasChave.add(chave);

    const janela = textoUpper.slice(Math.max(0, idx - 60), idx + 400);
    const datas = extrairDatas(janela);

    // Verifica keywords de data de reforço: "Revacinar", "Próxima", "Reforço"
    let dataProxima: string | null = null;
    const proxRE = /(?:REVACIN[A-Z]+|PR[OÓÔO]XIM[AO]|REFOR[CÇ]O)[:\s]*(\d{1,2}[\/\-\.]\d{1,2}[\/\-\.]\d{2,4})/;
    const proxMatch = proxRE.exec(janela);
    if (proxMatch) {
      const partes = proxMatch[1].split(/[\/\-\.]/).map(x => x.trim());
      if (partes.length === 3) {
        const [d2, m2, a2] = partes;
        const ano2 = a2.length === 2 ? "20" + a2 : a2;
        const iso2 = `${ano2}-${m2.padStart(2,"0")}-${d2.padStart(2,"0")}`;
        const dt2 = new Date(iso2);
        if (!isNaN(dt2.getTime())) dataProxima = iso2;
      }
    }

    const nomeBonito = nome.split(" ").map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(" ");

    encontradas.push({
      id: crypto.randomUUID(),
      nomeSugerido: nomeBonito,
      dataAplicada: datas[0] ?? null,
      dataProxima: dataProxima ?? datas[1] ?? null,
      incluir: true,
    });
  }

  return encontradas;
}

// ── Utilitários ──────────────────────────────────────────────────────────────

function rotuloFiltro(s: FiltroStatus) {
  return { TODOS: "Todas", APLICADA: "Aplicadas", PENDENTE: "Pendentes", EM_ATRASO: "Em atraso" }[s];
}

function formatarData(iso: string) {
  const [a, m, d] = iso.split("-");
  return `${d}/${m}/${a}`;
}

function gerarCertificadoPDF(carteira: Carteira) {
  const blocos = carteira.ciclos.map(c => {
    const linhas = c.doses.map(d => {
      const vis = STATUS_VISUAL[d.status] ?? STATUS_VISUAL["PENDENTE"];
      const dose = d.totalDoses > 1 ? `Dose ${d.doseNumero}/${d.totalDoses}` : "Dose única";
      const cor  = d.status === "APLICADA" ? "#059669" : d.status === "EM_ATRASO" ? "#dc2626" : "#d97706";
      return `<tr><td>${dose}</td><td style="color:${cor};font-weight:600">${vis.label}</td><td>${d.data ? formatarData(d.data) : "—"}</td><td>${d.medico ?? "—"}</td><td>${d.lote ?? "—"}</td></tr>`;
    }).join("");
    const protocolo = PROTOCOLO_INFO[c.tipoProtocolo] ?? PROTOCOLO_INFO["PERSONALIZADO"];
    const pct = c.totalDoses > 0 ? Math.round((c.aplicadas / c.totalDoses) * 100) : 0;
    return `<div class="ciclo"><div class="ciclo-header"><h3>${c.ciclo} <span class="badge-protocolo">${protocolo.label}</span></h3>
      <div class="progresso-wrap"><div class="barra-prog"><div class="barra-fill" style="width:${pct}%;background:${pct===100?"#059669":"#02AAB5"}"></div></div><span class="prog-label">${c.aplicadas}/${c.totalDoses} doses</span></div>
      ${c.dataProximaDoseSugerida && c.podeAgendarProxima ? `<p class="sugestao">📅 Próxima dose sugerida: <strong>${formatarData(c.dataProximaDoseSugerida)}</strong></p>` : ""}
      </div><table><thead><tr><th>Dose</th><th>Status</th><th>Data</th><th>Médico</th><th>Lote/Selo</th></tr></thead><tbody>${linhas}</tbody></table></div>`;
  }).join("");

  const html = `<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<title>Certificado de Vacinação — ${carteira.pacienteNome}</title>
<style>*{font-family:'Segoe UI',Arial,sans-serif;box-sizing:border-box}body{margin:32px;color:#1e293b;background:#fff}
.header{border-bottom:3px solid #02AAB5;padding-bottom:16px;margin-bottom:20px}.header h1{color:#02AAB5;margin:0 0 4px;font-size:22px}.header .sub{color:#64748b;font-size:13px;margin:0}
.paciente-box{background:#f0fdfc;border:1px solid #99f6e4;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:13px}
.ciclo{margin-bottom:28px}.ciclo-header{background:#f8fafc;border-radius:8px;padding:12px 16px;margin-bottom:8px}
.ciclo-header h3{margin:0 0 8px;color:#0f766e;font-size:16px;display:flex;align-items:center;gap:8px}
.badge-protocolo{font-size:11px;background:#e0f2fe;color:#0369a1;border-radius:9999px;padding:2px 8px;font-weight:500}
.progresso-wrap{display:flex;align-items:center;gap:10px}.barra-prog{flex:1;height:8px;background:#e2e8f0;border-radius:9999px;overflow:hidden}.barra-fill{height:100%;border-radius:9999px}
.prog-label{font-size:12px;color:#475569;font-weight:600;white-space:nowrap}.sugestao{margin:8px 0 0;font-size:12px;color:#0369a1;background:#eff6ff;border-radius:6px;padding:6px 10px}
table{width:100%;border-collapse:collapse;font-size:12px}th,td{border:1px solid #e2e8f0;padding:7px 10px;text-align:left}th{background:#f0fdfc;color:#0f766e;font-weight:600}tr:nth-child(even){background:#fafafa}
.footer{margin-top:24px;font-size:11px;color:#94a3b8;border-top:1px solid #e2e8f0;padding-top:12px}
</style></head><body>
<div class="header"><h1>🐾 petCollar — Certificado de Vacinação</h1><p class="sub">Emitido em ${new Date().toLocaleDateString("pt-BR")} · Documento digital</p></div>
<div class="paciente-box"><strong>Paciente:</strong> ${carteira.pacienteNome} &nbsp;|&nbsp; <strong>Espécie:</strong> ${carteira.especie} &nbsp;|&nbsp; <strong>Raça:</strong> ${carteira.raca}</div>
${blocos || "<p>Nenhuma vacina registrada.</p>"}
<p class="footer">Documento gerado automaticamente pelo sistema petCollar. Para salvar como PDF, selecione "Salvar como PDF" ao imprimir.</p>
<script>window.onload=()=>window.print()</script></body></html>`;

  const win = window.open("", "_blank");
  if (!win) { alert("Permita pop-ups para baixar o certificado."); return; }
  win.document.write(html); win.document.close();
}
