import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  type ProntuarioDTO,
  type VacinaAplicadaDTO,
  type VacinaPendenteDTO,
} from "./medicoService";

// ── Tela dedicada de Vacinação (F-06) ──────────────────────────────────────────
// Centraliza a aplicação de doses vacinais e o histórico do paciente, espelhando
// o layout do Relatório Clínico Evolutivo. Diferente do relatório, não coleta
// sinais vitais — apenas registra a data do atendimento e a dose aplicada.

export function MedicoVacinacao() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const navigate = useNavigate();
  const { apiFetch, session } = useAuth();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  const [prontuario, setProntuario] = useState<ProntuarioDTO | null>(null);
  const [carregando, setCarregando] = useState(true);

  const [vacinasPendentes, setVacinasPendentes] = useState<VacinaPendenteDTO[]>([]);
  const [vacinasAplicadas, setVacinasAplicadas] = useState<VacinaAplicadaDTO[]>([]);
  const [doseSelecionada, setDoseSelecionada] = useState("");
  const [loteVacina, setLoteVacina] = useState("");
  const [aplicandoVacina, setAplicandoVacina] = useState(false);
  const [vacinaMsg, setVacinaMsg] = useState<string | null>(null);

  const hoje = new Date().toLocaleDateString("pt-BR");
  const medicoNome = session?.user.nome ?? session?.user.identificador ?? "Médico";

  useEffect(() => {
    if (!pacienteId) return;
    Promise.all([
      service.buscarProntuario(pacienteId),
      service.listarVacinasPendentes(pacienteId).catch(() => [] as VacinaPendenteDTO[]),
      service.listarVacinasAplicadas(pacienteId).catch(() => [] as VacinaAplicadaDTO[]),
    ])
      .then(([p, pend, aplic]) => {
        setProntuario(p);
        setVacinasPendentes(pend);
        setVacinasAplicadas(aplic);
      })
      .finally(() => setCarregando(false));
  }, [service, pacienteId]);

  async function aplicarVacina() {
    if (!pacienteId || !doseSelecionada) return;
    const vac = vacinasPendentes.find((v) => v.doseId === doseSelecionada);
    if (!vac) return;
    setAplicandoVacina(true);
    setVacinaMsg(null);
    try {
      await service.aplicarVacina(pacienteId, vac.cicloId, vac.doseId, loteVacina);
      setVacinasPendentes((prev) => prev.filter((v) => v.doseId !== vac.doseId));
      setDoseSelecionada("");
      setLoteVacina("");
      setVacinaMsg(`✓ ${vac.rotulo} aplicada e registrada na carteira do tutor.`);
      // Recarrega o histórico para refletir a dose recém-aplicada.
      service.listarVacinasAplicadas(pacienteId).then(setVacinasAplicadas).catch(() => {});
    } catch (err) {
      setVacinaMsg((err as Error).message || "Não foi possível aplicar a vacina.");
    } finally {
      setAplicandoVacina(false);
    }
  }

  if (carregando) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-64 animate-pulse rounded-xl bg-ink-100" />
        <div className="card h-24 animate-pulse" />
        <div className="card h-40 animate-pulse" />
        <div className="card h-60 animate-pulse" />
      </div>
    );
  }

  if (!prontuario) {
    return (
      <div>
        <button onClick={() => navigate(`/medico/prontuario/${pacienteId}`)} className="btn-ghost mb-4 text-sm">
          ← Voltar ao Prontuário
        </button>
        <div role="alert" className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          Prontuário não encontrado.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">

      {/* ── Cabeçalho ──────────────────────────────────────────────────────── */}
      <div className="flex items-center justify-between">
        <button onClick={() => navigate(`/medico/prontuario/${pacienteId}`)} className="btn-ghost text-sm">
          ← Voltar ao Prontuário
        </button>
        <div className="text-center">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">
            Vacinação
          </p>
          <h1 className="text-xl font-bold text-ink-900">Aplicação de Vacina</h1>
        </div>
        <div className="w-40" />
      </div>

      {/* ── Banner do paciente ──────────────────────────────────────────────── */}
      <div className="rounded-2xl border border-brand-200 bg-gradient-to-r from-brand-50 to-white p-4 flex items-center gap-4">
        <div className="h-12 w-12 rounded-2xl bg-brand-100 flex items-center justify-center text-2xl shrink-0">
          💉
        </div>
        <div className="min-w-0 flex-1">
          <p className="font-bold text-ink-900 text-base">{prontuario.nomePet}</p>
          <p className="text-xs text-ink-500">
            {prontuario.especie} · {prontuario.raca}
            {prontuario.idadeAnos > 0 && ` · ${prontuario.idadeAnos} anos`}
          </p>
          <p className="text-xs text-ink-500">
            Tutor: <span className="font-medium text-ink-700">{prontuario.nomeTutor}</span>
          </p>
        </div>
        <div className="text-right shrink-0">
          <p className="text-xs text-ink-400">Data</p>
          <p className="text-sm font-semibold text-ink-900">{hoje}</p>
          <p className="mt-0.5 max-w-[140px] truncate text-xs text-ink-500">{medicoNome}</p>
        </div>
      </div>

      {/* ── Alerta de alergias ─────────────────────────────────────────────── */}
      {prontuario.alergias.length > 0 && (
        <div role="alert" className="rounded-2xl border-2 border-red-300 bg-red-50 p-4">
          <div className="flex items-center gap-2 mb-3">
            <span className="text-lg">⚠️</span>
            <h2 className="font-bold text-red-800 text-sm">
              Alergias Conhecidas — Atenção antes de aplicar (RN-125)
            </h2>
          </div>
          <div className="flex flex-wrap gap-2">
            {prontuario.alergias.map((a, i) => (
              <span
                key={i}
                className="inline-flex items-center rounded-full bg-red-100 px-3 py-0.5 text-xs font-medium text-red-800 ring-1 ring-red-300"
              >
                ⊘ {a}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* ── Dados do atendimento (sem sinais vitais) ─────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Dados do Atendimento</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label">Data da aplicação</label>
            <div className="input cursor-default bg-ink-100/60 text-ink-600">{hoje}</div>
          </div>
          <div>
            <label className="label">Médico Responsável</label>
            <div className="input cursor-default bg-ink-100/60 text-ink-600">{medicoNome}</div>
          </div>
        </div>
      </div>

      {/* ── Aplicar Vacina ───────────────────────────────────────────────────── */}
      <div className="card border-l-4 border-brand-400 p-6">
        <div className="mb-1 flex items-center gap-2">
          <span className="text-lg text-brand-600">💉</span>
          <h2 className="text-base font-semibold text-ink-900">Aplicação de Vacina</h2>
        </div>
        <p className="mb-4 text-xs text-ink-500">
          Selecione uma dose pendente da carteira do paciente. Ao aplicar, o status é
          atualizado na carteira de vacinação vista pelo tutor (F-06).
        </p>

        {vacinasPendentes.length === 0 ? (
          <p className="rounded-xl bg-ink-50 px-4 py-3 text-sm text-ink-500">
            Nenhuma dose pendente na carteira deste paciente. O tutor cadastra as vacinas
            planejadas no portal.
          </p>
        ) : (
          <div className="grid gap-3 sm:grid-cols-[1fr_auto_auto] sm:items-end">
            <div>
              <label className="label" htmlFor="dose">Vacina pendente</label>
              <select
                id="dose"
                value={doseSelecionada}
                onChange={(e) => setDoseSelecionada(e.target.value)}
                className="input"
              >
                <option value="">Selecione…</option>
                {vacinasPendentes.map((v) => (
                  <option key={v.doseId} value={v.doseId}>
                    {v.rotulo}{v.status === "EM_ATRASO" ? " — em atraso" : ""}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label" htmlFor="lote">Lote / Selo</label>
              <input
                id="lote"
                type="text"
                value={loteVacina}
                onChange={(e) => setLoteVacina(e.target.value)}
                className="input"
                placeholder="Ex: LT-2026-A"
              />
            </div>
            <button
              type="button"
              onClick={aplicarVacina}
              disabled={!doseSelecionada || aplicandoVacina}
              className="btn-primary sm:w-auto"
            >
              {aplicandoVacina ? "Aplicando..." : "Aplicar"}
            </button>
          </div>
        )}

        {vacinaMsg && (
          <p className={"mt-3 text-sm " + (vacinaMsg.startsWith("✓") ? "text-brand-700" : "text-red-700")}>
            {vacinaMsg}
          </p>
        )}
      </div>

      {/* ── Histórico de vacinas aplicadas ───────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-1 text-base font-semibold text-ink-900">Histórico de Vacinas Aplicadas</h2>
        <p className="mb-4 text-xs text-ink-500">Doses já aplicadas na carteira do paciente (F-06)</p>
        {vacinasAplicadas.length === 0 ? (
          <p className="text-sm text-ink-500">Nenhuma vacina aplicada registrada para este paciente.</p>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-ink-100">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-ink-100 bg-ink-50/70">
                  {["Data", "Vacina", "Médico", "Lote / Selo"].map((col) => (
                    <th
                      key={col}
                      className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wider text-ink-500"
                    >
                      {col}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {vacinasAplicadas.map((v) => (
                  <tr key={v.doseId} className="hover:bg-ink-50/40">
                    <td className="px-4 py-2.5 text-ink-600">{formatarData(v.dataAplicacao)}</td>
                    <td className="px-4 py-2.5 font-medium text-ink-900">{v.rotulo}</td>
                    <td className="px-4 py-2.5 text-ink-800">{v.medico || "—"}</td>
                    <td className="px-4 py-2.5 text-ink-800">{v.lote || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Rodapé: concluir ─────────────────────────────────────────────────── */}
      <div className="rounded-2xl border border-dashed border-ink-300/70 bg-white/90 p-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-xs text-ink-500">
          As doses aplicadas refletem imediatamente na carteira de vacinação do tutor.
        </p>
        <button
          type="button"
          onClick={() => navigate(`/medico/prontuario/${pacienteId}`)}
          className="btn-primary shrink-0 sm:w-auto"
        >
          ✓ Concluir
        </button>
      </div>

    </div>
  );
}

// ── Utilitário ─────────────────────────────────────────────────────────────────

function formatarData(iso: string): string {
  // Datas vêm como ISO (AAAA-MM-DD); converte para o formato brasileiro.
  const partes = iso.slice(0, 10).split("-");
  if (partes.length === 3) {
    const [a, m, d] = partes;
    return `${d}/${m}/${a}`;
  }
  return iso;
}
