import { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import type { Paciente } from "./TutorInicio";

type StatusVacina = "APLICADA" | "PENDENTE" | "EM_ATRASO";

type Dose = {
  id: string;
  ciclo: string;
  rotulo: string;
  doseNumero: number | null;
  totalDoses: number | null;
  status: StatusVacina;
  data: string | null;
  medico: string | null;
  lote: string | null;
};

type Ciclo = {
  ciclo: string;
  totalDoses: number;
  aplicadas: number;
  registradas: number;
  podeAgendarProxima: boolean;
  doses: Dose[];
};

type Carteira = {
  pacienteNome: string;
  especie: string;
  raca: string;
  ciclos: Ciclo[];
};

type FiltroStatus = "TODOS" | StatusVacina;

export function TutorVacinacao() {
  const { apiFetch } = useAuth();
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [pacienteId, setPacienteId] = useState<string>("");
  const [carteira, setCarteira] = useState<Carteira | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  const [filtroStatus, setFiltroStatus] = useState<FiltroStatus>("TODOS");
  const [soCompletos, setSoCompletos] = useState(false);

  const [agendarProximaCiclo, setAgendarProximaCiclo] = useState<Ciclo | null>(null);
  const [agendarNova, setAgendarNova] = useState(false);

  useEffect(() => {
    apiFetch("/api/tutor/pacientes")
      .then(r => r.json())
      .then((ps: Paciente[]) => {
        setPacientes(ps);
        if (ps.length > 0) setPacienteId(ps[0].id);
      })
      .catch(() => setErro("Falha ao carregar pacientes."));
  }, [apiFetch]);

  const carregarCarteira = useCallback(async () => {
    if (!pacienteId) return;
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas`);
      if (!res.ok) throw new Error(`Falha ao carregar carteira (HTTP ${res.status}).`);
      setCarteira(await res.json());
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [apiFetch, pacienteId]);

  useEffect(() => { void carregarCarteira(); }, [carregarCarteira]);

  const ciclosFiltrados = useMemo(() => {
    let ciclos = carteira?.ciclos ?? [];
    if (soCompletos) ciclos = ciclos.filter(c => c.aplicadas >= c.totalDoses);
    if (filtroStatus !== "TODOS") {
      ciclos = ciclos
        .map(c => ({ ...c, doses: c.doses.filter(d => d.status === filtroStatus) }))
        .filter(c => c.doses.length > 0);
    }
    return ciclos;
  }, [carteira, filtroStatus, soCompletos]);

  function baixarCertificado() {
    if (carteira) gerarCertificadoPDF(carteira);
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-ink-900">Carteira Digital de Vacinação</h1>
        <p className="text-sm text-ink-500">Acompanhe doses aplicadas, pendências e agende novas vacinas.</p>
      </div>

      <div className="mb-6 flex flex-wrap items-end gap-4">
        <div className="min-w-[260px]">
          <label className="label" htmlFor="paciente">Selecione o paciente</label>
          <select id="paciente" className="input" value={pacienteId} onChange={e => setPacienteId(e.target.value)}>
            {pacientes.length === 0 && <option value="">Nenhum paciente cadastrado</option>}
            {pacientes.map(p => (
              <option key={p.id} value={p.id}>{p.nome} — {p.especie}, {p.raca}</option>
            ))}
          </select>
        </div>

        {carteira && (
          <div className="flex flex-1 flex-wrap items-center justify-end gap-2">
            <button onClick={() => setAgendarNova(true)} className="btn-ghost ring-1 ring-ink-300">
              + Agendar nova vacina
            </button>
            <button onClick={baixarCertificado} className="btn-primary w-auto">
              ⬇ Baixar certificado (PDF)
            </button>
          </div>
        )}
      </div>

      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
          {erro}
        </div>
      )}

      {carregando && <p className="text-sm text-ink-500">Carregando carteira…</p>}

      {carteira && !carregando && (
        <>
          <section className="mb-4 flex flex-wrap items-center gap-3">
            <div className="inline-flex rounded-xl bg-white p-1 ring-1 ring-ink-300/60 shadow-sm">
              {(["TODOS", "APLICADA", "PENDENTE", "EM_ATRASO"] as FiltroStatus[]).map(s => (
                <button
                  key={s}
                  onClick={() => setFiltroStatus(s)}
                  className={
                    "rounded-lg px-3 py-1.5 text-sm font-medium transition " +
                    (filtroStatus === s ? "bg-brand-500 text-white shadow" : "text-ink-700 hover:bg-ink-100")
                  }
                >
                  {rotuloFiltro(s)}
                </button>
              ))}
            </div>
            <label className="inline-flex items-center gap-2 text-sm text-ink-700">
              <input
                type="checkbox"
                className="h-4 w-4 rounded border-ink-300 text-brand-500 focus:ring-brand-500"
                checked={soCompletos}
                onChange={e => setSoCompletos(e.target.checked)}
              />
              Apenas ciclos com todas as doses tomadas
            </label>
          </section>

          {ciclosFiltrados.length === 0 ? (
            <div className="card px-6 py-12 text-center text-sm text-ink-500">
              Nenhuma vacina para o filtro selecionado.
            </div>
          ) : (
            <div className="space-y-4">
              {ciclosFiltrados.map(c => (
                <CicloCard
                  key={c.ciclo}
                  ciclo={c}
                  onAgendarProxima={() => setAgendarProximaCiclo(c)}
                />
              ))}
            </div>
          )}
        </>
      )}

      {agendarProximaCiclo && (
        <AgendarProximaModal
          ciclo={agendarProximaCiclo}
          pacienteId={pacienteId}
          onFechar={() => setAgendarProximaCiclo(null)}
          onAgendado={() => { setAgendarProximaCiclo(null); void carregarCarteira(); }}
        />
      )}

      {agendarNova && (
        <AgendarNovaModal
          pacienteId={pacienteId}
          onFechar={() => setAgendarNova(false)}
          onAgendado={() => { setAgendarNova(false); void carregarCarteira(); }}
        />
      )}
    </div>
  );
}

function rotuloFiltro(s: FiltroStatus) {
  return { TODOS: "Todas", APLICADA: "Aplicadas", PENDENTE: "Pendentes", EM_ATRASO: "Em atraso" }[s];
}

const ESTILO_STATUS: Record<StatusVacina, { dose: string; badge: string; label: string }> = {
  APLICADA: { dose: "border-emerald-200 bg-emerald-50/50", badge: "bg-emerald-100 text-emerald-700", label: "Aplicada" },
  PENDENTE: { dose: "border-amber-200 bg-amber-50/60", badge: "bg-amber-100 text-amber-800", label: "Pendente" },
  EM_ATRASO: { dose: "border-paw-200 bg-paw-50/60", badge: "bg-paw-100 text-paw-700", label: "Em Atraso" },
};

function CicloCard({ ciclo, onAgendarProxima }: { ciclo: Ciclo; onAgendarProxima: () => void }) {
  const completo = ciclo.aplicadas >= ciclo.totalDoses;
  const pct = ciclo.totalDoses > 0 ? Math.round((ciclo.aplicadas / ciclo.totalDoses) * 100) : 0;

  return (
    <div className="card overflow-hidden">
      {/* Cabeçalho do ciclo com barra de progresso */}
      <div className="border-b border-ink-300/40 bg-ink-100/40 p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <h3 className="text-lg font-bold text-ink-900">{ciclo.ciclo}</h3>
            {completo && (
              <span className="inline-flex rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
                Ciclo completo ✓
              </span>
            )}
          </div>
          {ciclo.podeAgendarProxima && (
            <button onClick={onAgendarProxima} className="btn-ghost ring-1 ring-ink-300">
              Agendar próxima dose
            </button>
          )}
        </div>

        <div className="mt-3 flex items-center gap-3">
          <div className="h-3 flex-1 overflow-hidden rounded-full bg-ink-100">
            <div
              className={"h-full rounded-full transition-all " + (completo ? "bg-emerald-500" : "bg-brand-500")}
              style={{ width: `${pct}%` }}
            />
          </div>
          <span className={"shrink-0 text-sm font-medium " + (completo ? "text-emerald-700" : "text-ink-600")}>
            {ciclo.aplicadas} de {ciclo.totalDoses} doses
          </span>
        </div>
      </div>

      {/* Doses do ciclo (indentadas) */}
      <ul className="divide-y divide-ink-300/30">
        {ciclo.doses.map(d => (
          <DoseLinha key={d.id} dose={d} />
        ))}
      </ul>
    </div>
  );
}

function DoseLinha({ dose }: { dose: Dose }) {
  const estilo = ESTILO_STATUS[dose.status];
  return (
    <li className={`flex flex-wrap items-center gap-x-6 gap-y-1 border-l-4 px-5 py-3 ${estilo.dose}`}>
      <div className="flex min-w-[180px] items-center gap-2">
        <span className="font-semibold text-ink-900">
          {dose.doseNumero && dose.totalDoses ? `Dose ${dose.doseNumero}/${dose.totalDoses}` : "Dose única"}
        </span>
        <span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-semibold ${estilo.badge}`}>
          {estilo.label}
        </span>
      </div>
      <Info rotulo="Data" valor={dose.data ? formatarData(dose.data) : "—"} />
      {dose.medico && <Info rotulo="Médico" valor={dose.medico} />}
      {dose.lote && <Info rotulo="Lote/Selo" valor={dose.lote} />}
    </li>
  );
}

function Info({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div className="flex gap-1.5 text-sm">
      <span className="font-semibold text-ink-800">{rotulo}:</span>
      <span className="text-ink-700">{valor}</span>
    </div>
  );
}

function AgendarProximaModal({
  ciclo, pacienteId, onFechar, onAgendado,
}: { ciclo: Ciclo; pacienteId: string; onFechar: () => void; onAgendado: () => void }) {
  const { apiFetch } = useAuth();
  const [data, setData] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const proximaNumero = ciclo.registradas + 1;

  async function confirmar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas/proxima-dose`, {
        method: "POST",
        body: JSON.stringify({ ciclo: ciclo.ciclo, data }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha ao agendar (HTTP ${res.status}).`);
      }
      onAgendado();
    } catch (e2) {
      setErro((e2 as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <ModalShell titulo="Agendar próxima dose" onFechar={onFechar}>
      <p className="mb-4 rounded-lg bg-brand-50 px-3 py-2 text-sm text-brand-800 ring-1 ring-brand-100">
        Ciclo <strong>{ciclo.ciclo}</strong> — agendando a <strong>Dose {proximaNumero}/{ciclo.totalDoses}</strong>.
      </p>
      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{erro}</div>
      )}
      <form onSubmit={confirmar} className="grid gap-4">
        <div>
          <label className="label" htmlFor="dataProx">Data do agendamento</label>
          <input id="dataProx" type="date" required className="input"
                 min={new Date().toISOString().slice(0, 10)}
                 value={data} onChange={e => setData(e.target.value)} />
        </div>
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
          <button type="submit" disabled={enviando} className="btn-primary w-auto">
            {enviando ? "Agendando…" : "Confirmar"}
          </button>
        </div>
      </form>
    </ModalShell>
  );
}

function AgendarNovaModal({
  pacienteId, onFechar, onAgendado,
}: { pacienteId: string; onFechar: () => void; onAgendado: () => void }) {
  const { apiFetch } = useAuth();
  const [ciclo, setCiclo] = useState("");
  const [totalDoses, setTotalDoses] = useState(1);
  const [data, setData] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function confirmar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      const res = await apiFetch(`/api/tutor/pacientes/${pacienteId}/vacinas`, {
        method: "POST",
        body: JSON.stringify({ ciclo, totalDoses, data }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha ao agendar (HTTP ${res.status}).`);
      }
      onAgendado();
    } catch (e2) {
      setErro((e2 as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <ModalShell titulo="Agendar nova vacina" onFechar={onFechar}>
      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{erro}</div>
      )}
      <form onSubmit={confirmar} className="grid gap-4">
        <div>
          <label className="label" htmlFor="nomeVacina">Nome da vacina (ciclo)</label>
          <input id="nomeVacina" required className="input" placeholder="Ex.: V10, Antirrábica, Giardíase"
                 value={ciclo} onChange={e => setCiclo(e.target.value)} />
        </div>
        <div>
          <label className="label" htmlFor="totalDoses">Total de doses do ciclo</label>
          <input id="totalDoses" type="number" min={1} max={10} required className="input"
                 value={totalDoses} onChange={e => setTotalDoses(Number(e.target.value))} />
          <p className="mt-1 text-xs text-ink-500">
            Use 1 para dose única. As próximas doses serão agendadas pelo botão "Agendar próxima dose".
          </p>
        </div>
        <div>
          <label className="label" htmlFor="dataNova">Data da 1ª dose</label>
          <input id="dataNova" type="date" required className="input"
                 min={new Date().toISOString().slice(0, 10)}
                 value={data} onChange={e => setData(e.target.value)} />
        </div>
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
          <button type="submit" disabled={enviando} className="btn-primary w-auto">
            {enviando ? "Agendando…" : "Confirmar"}
          </button>
        </div>
      </form>
    </ModalShell>
  );
}

function ModalShell({
  titulo, children, onFechar,
}: { titulo: string; children: React.ReactNode; onFechar: () => void }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="w-full max-w-md card p-6" onClick={e => e.stopPropagation()}>
        <h3 className="mb-5 text-lg font-bold text-ink-900">{titulo}</h3>
        {children}
      </div>
    </div>
  );
}

function formatarData(iso: string) {
  const [a, m, d] = iso.split("-");
  return `${d}/${m}/${a}`;
}

function gerarCertificadoPDF(carteira: Carteira) {
  const blocos = carteira.ciclos
    .map(c => {
      const linhas = c.doses
        .map(d => {
          const status = ESTILO_STATUS[d.status].label;
          const dose = d.doseNumero && d.totalDoses ? `Dose ${d.doseNumero}/${d.totalDoses}` : "Dose única";
          return `<tr>
            <td>${dose}</td>
            <td>${status}</td>
            <td>${d.data ? formatarData(d.data) : "—"}</td>
            <td>${d.medico ?? "—"}</td>
            <td>${d.lote ?? "—"}</td>
          </tr>`;
        })
        .join("");
      const completo = c.aplicadas >= c.totalDoses;
      return `<div class="ciclo">
        <h3>${c.ciclo} <span class="prog">${c.aplicadas}/${c.totalDoses} doses${completo ? " ✓" : ""}</span></h3>
        <table>
          <thead><tr><th>Dose</th><th>Status</th><th>Data</th><th>Médico</th><th>Lote/Selo</th></tr></thead>
          <tbody>${linhas}</tbody>
        </table>
      </div>`;
    })
    .join("");

  const html = `<!doctype html>
<html lang="pt-BR"><head><meta charset="utf-8"><title>Certificado de Vacinação — ${carteira.pacienteNome}</title>
<style>
  * { font-family: Arial, Helvetica, sans-serif; }
  body { margin: 40px; color: #2e2e2e; }
  h1 { color: #02AAB5; margin-bottom: 4px; }
  .sub { color: #6b7280; margin-top: 0; }
  .box { border: 1px solid #cdcdcd; border-radius: 8px; padding: 12px 16px; margin: 16px 0; }
  .ciclo { margin-top: 18px; }
  .ciclo h3 { color: #06717b; margin: 0 0 6px; }
  .prog { font-size: 12px; color: #6b7280; font-weight: normal; }
  table { width: 100%; border-collapse: collapse; }
  th, td { border: 1px solid #cdcdcd; padding: 7px 10px; text-align: left; font-size: 13px; }
  th { background: #e7fbfc; color: #06717b; }
  .rodape { margin-top: 28px; font-size: 11px; color: #9aa3af; }
</style></head>
<body>
  <h1>🐾 petCollar — Certificado de Vacinação</h1>
  <p class="sub">Carteira digital emitida em ${new Date().toLocaleDateString("pt-BR")}</p>
  <div class="box">
    <strong>Paciente:</strong> ${carteira.pacienteNome} &nbsp;|&nbsp;
    <strong>Espécie:</strong> ${carteira.especie} &nbsp;|&nbsp;
    <strong>Raça:</strong> ${carteira.raca}
  </div>
  ${blocos || "<p>Nenhuma vacina registrada.</p>"}
  <p class="rodape">Documento gerado automaticamente pelo sistema petCollar. Para salvar como PDF, escolha o destino "Salvar como PDF" na caixa de impressão.</p>
  <script>window.onload = () => window.print();</script>
</body></html>`;

  const win = window.open("", "_blank");
  if (!win) { alert("Permita pop-ups para baixar o certificado."); return; }
  win.document.write(html);
  win.document.close();
}
