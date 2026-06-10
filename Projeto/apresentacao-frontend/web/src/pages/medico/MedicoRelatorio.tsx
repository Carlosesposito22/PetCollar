import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  MEDICAMENTOS_STUB,
  type ProntuarioDTO,
  type RegistroHistoricoDTO,
  type TipoRelatorio,
  type VacinaPendenteDTO,
} from "./medicoService";
import { SignaturePad, type SignaturePadHandle } from "./SignaturePad";
import {
  gerarPdfRelatorio,
  relatorioDaTriagem,
  salvarRelatorio,
  type RelatorioSalvo,
} from "./relatorioStorage";

// ── Padrão Strategy: validação de completude por tipo de relatório ─────────────
// Cada tipo define suas próprias regras de preenchimento obrigatório antes da
// assinatura digital, espelhando IValidadorRelatorioStrategy do domínio (RN-124).

type CamposFormulario = {
  diagnostico: string;
  resumoTutor: string;
  cuidadosPosOp: string;
  tempoRecuperacao: string;
};

type ConfiguracaoTipoRelatorio = {
  rotulo: string;
  descricao: string;
  icone: string;
  validar(campos: CamposFormulario): string | null;
};

const CONFIGURACOES_TIPO: Record<TipoRelatorio, ConfiguracaoTipoRelatorio> = {
  ROTINEIRO: {
    rotulo: "Consulta Rotineira",
    descricao: "Acompanhamento, doença crônica ou retorno",
    icone: "🩺",
    validar({ diagnostico, resumoTutor }) {
      if (!diagnostico.trim()) return "O diagnóstico técnico é obrigatório para consulta rotineira.";
      if (!resumoTutor.trim()) return "O resumo para o tutor é obrigatório.";
      return null;
    },
  },
  CIRURGICO: {
    rotulo: "Procedimento Cirúrgico",
    descricao: "Cirurgia ou procedimento invasivo com anestesia",
    icone: "🔬",
    validar({ diagnostico, resumoTutor, cuidadosPosOp, tempoRecuperacao }) {
      if (!diagnostico.trim()) return "O diagnóstico técnico é obrigatório.";
      if (!resumoTutor.trim()) return "O resumo para o tutor é obrigatório.";
      if (!cuidadosPosOp.trim()) return "Os cuidados pós-operatórios são obrigatórios para relatório cirúrgico.";
      if (!tempoRecuperacao.trim()) return "O tempo de recuperação estimado é obrigatório para relatório cirúrgico.";
      return null;
    },
  },
  PREVENTIVO: {
    rotulo: "Consulta Preventiva",
    descricao: "Vacinação, vermifugação ou check-up de rotina",
    icone: "💉",
    validar({ resumoTutor }) {
      if (!resumoTutor.trim()) return "O resumo para o tutor é obrigatório.";
      return null;
    },
  },
};

// ── Componente Principal ───────────────────────────────────────────────────────

export function MedicoRelatorio() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const [searchParams] = useSearchParams();
  const triagemId = searchParams.get("triagem") ?? "";
  const navigate = useNavigate();
  const { apiFetch, session } = useAuth();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  const [prontuario, setProntuario] = useState<ProntuarioDTO | null>(null);
  const [historico, setHistorico] = useState<RegistroHistoricoDTO[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [assinado, setAssinado] = useState(false);
  const [assinando, setAssinando] = useState(false);
  const [erroAssinatura, setErroAssinatura] = useState<string | null>(null);

  const [tipoRelatorio, setTipoRelatorio] = useState<TipoRelatorio>("ROTINEIRO");
  const [peso, setPeso] = useState("");
  const [temperatura, setTemperatura] = useState("");
  const [frequenciaCardiaca, setFrequenciaCardiaca] = useState("");
  const [diagnostico, setDiagnostico] = useState("");
  const [resumoTutor, setResumoTutor] = useState("");
  const [orientacoes, setOrientacoes] = useState("");
  const [cuidadosPosOp, setCuidadosPosOp] = useState("");
  const [tempoRecuperacao, setTempoRecuperacao] = useState("");
  const [arquivosSelecionados, setArquivosSelecionados] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const assinaturaRef = useRef<SignaturePadHandle>(null);
  const [assinaturaVazia, setAssinaturaVazia] = useState(true);
  const [relatorioSalvo, setRelatorioSalvo] = useState<RelatorioSalvo | null>(null);
  const [jaEmitido, setJaEmitido] = useState(false);

  // Vacinas (apenas consulta preventiva)
  const [vacinasPendentes, setVacinasPendentes] = useState<VacinaPendenteDTO[]>([]);
  const [doseSelecionada, setDoseSelecionada] = useState("");
  const [loteVacina, setLoteVacina] = useState("");
  const [aplicandoVacina, setAplicandoVacina] = useState(false);
  const [vacinaMsg, setVacinaMsg] = useState<string | null>(null);
  const [vacinasAplicadas, setVacinasAplicadas] = useState<string[]>([]);

  const hoje = new Date().toLocaleDateString("pt-BR");
  const medicoNome = session?.user.nome ?? session?.user.identificador ?? "Médico";

  useEffect(() => {
    if (!pacienteId) return;
    // Um relatório por atendimento (triagem): se já existe, abre em leitura.
    const existente = triagemId ? relatorioDaTriagem(pacienteId, triagemId) : null;
    Promise.all([
      service.buscarProntuario(pacienteId),
      service.buscarHistoricoComparativo(pacienteId).catch(() => [] as RegistroHistoricoDTO[]),
    ])
      .then(([p, h]) => {
        setProntuario(p);
        setHistorico(h);
        if (existente) {
          setRelatorioSalvo(existente);
          setTipoRelatorio(existente.tipoRelatorio as TipoRelatorio);
          setPeso(existente.peso);
          setTemperatura(existente.temperatura);
          setFrequenciaCardiaca(existente.frequenciaCardiaca);
          setDiagnostico(existente.diagnostico);
          setResumoTutor(existente.resumoTutor);
          setOrientacoes(existente.orientacoes);
          setCuidadosPosOp(existente.cuidadosPosOp);
          setTempoRecuperacao(existente.tempoRecuperacao);
          setJaEmitido(true);
          setAssinado(true);
        }
      })
      .finally(() => setCarregando(false));
  }, [service, pacienteId, triagemId]);

  // Carrega vacinas pendentes quando o tipo é preventivo
  useEffect(() => {
    if (!pacienteId || tipoRelatorio !== "PREVENTIVO" || assinado) return;
    service.listarVacinasPendentes(pacienteId)
      .then(setVacinasPendentes)
      .catch(() => setVacinasPendentes([]));
  }, [service, pacienteId, tipoRelatorio, assinado]);

  function handleArquivos(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    setArquivosSelecionados((prev) => [...prev, ...files].slice(0, 4));
  }

  function removerArquivo(index: number) {
    setArquivosSelecionados((prev) => prev.filter((_, i) => i !== index));
  }

  async function aplicarVacina() {
    if (!pacienteId || !doseSelecionada) return;
    const vac = vacinasPendentes.find((v) => v.doseId === doseSelecionada);
    if (!vac) return;
    setAplicandoVacina(true);
    setVacinaMsg(null);
    try {
      await service.aplicarVacina(pacienteId, vac.cicloId, vac.doseId, loteVacina);
      setVacinasAplicadas((prev) => [...prev, `${vac.rotulo} (lote ${loteVacina || "—"})`]);
      setVacinasPendentes((prev) => prev.filter((v) => v.doseId !== vac.doseId));
      setDoseSelecionada("");
      setLoteVacina("");
      setVacinaMsg(`✓ ${vac.rotulo} aplicada e registrada na carteira do tutor.`);
    } catch (err) {
      setVacinaMsg((err as Error).message || "Não foi possível aplicar a vacina.");
    } finally {
      setAplicandoVacina(false);
    }
  }

  async function handleAssinar(e: React.FormEvent) {
    e.preventDefault();
    // Sinais vitais são obrigatórios em qualquer tipo de relatório.
    if (!peso.trim() || !temperatura.trim() || !frequenciaCardiaca.trim()) {
      setErroAssinatura("Preencha peso, temperatura e frequência cardíaca antes de publicar.");
      return;
    }
    const estrategia = CONFIGURACOES_TIPO[tipoRelatorio];
    const erro = estrategia.validar({ diagnostico, resumoTutor, cuidadosPosOp, tempoRecuperacao });
    if (erro) { setErroAssinatura(erro); return; }
    if (assinaturaRef.current?.estaVazio()) {
      setErroAssinatura("Desenhe sua assinatura no quadro antes de publicar.");
      return;
    }
    setAssinando(true);
    setErroAssinatura(null);

    const assinaturaDataUrl = assinaturaRef.current?.toDataURL() ?? "";

    // Consome a API do backend: inicia o relatório, registra sinais vitais e conteúdo,
    // e assina digitalmente (a validação Strategy roda no domínio). Best-effort: se a
    // API falhar (ex.: backend em memória reiniciado), seguimos com a cópia local.
    if (pacienteId) {
      try {
        const atendimentoId =
          (crypto as any).randomUUID?.() ?? `atend-${Date.now()}`;
        const medicoId = session?.user.identificador ?? "medico";
        const rel = await service.iniciarRelatorio({
          atendimentoId, pacienteId, medicoId, tipoRelatorio,
        });
        await service.registrarSinaisVitais(
          rel.id,
          peso ? parseFloat(peso) : 0,
          temperatura ? parseFloat(temperatura) : 0,
        );
        await service.atualizarConteudo(rel.id, {
          diagnosticoTecnico: diagnostico || undefined,
          resumoParaTutor: resumoTutor || undefined,
          orientacoesManejo: orientacoes || undefined,
          cuidadosPosOperatorios: cuidadosPosOp || undefined,
          tempoRecuperacaoEstimado: tempoRecuperacao || undefined,
        });
        await service.assinarRelatorio(rel.id);
      } catch {
        // segue com a persistência local mesmo se o backend recusar
      }
    }

    const preventivo = tipoRelatorio === "PREVENTIVO";
    const salvo: RelatorioSalvo = {
      id: (crypto as any).randomUUID?.() ?? `rel-${Date.now()}`,
      pacienteId: pacienteId ?? "",
      triagemId,
      nomePet: prontuario?.nomePet ?? "Paciente",
      nomeTutor: prontuario?.nomeTutor ?? "—",
      especie: prontuario?.especie ?? "—",
      raca: prontuario?.raca ?? "—",
      medicoNome,
      data: hoje,
      tipoRelatorio,
      tipoRotulo: CONFIGURACOES_TIPO[tipoRelatorio].rotulo,
      peso, temperatura, frequenciaCardiaca,
      diagnostico, resumoTutor, orientacoes,
      cuidadosPosOp, tempoRecuperacao,
      medicamentos: preventivo ? [] : MEDICAMENTOS_STUB,
      anexos: preventivo ? [] : arquivosSelecionados.map((f) => f.name),
      assinaturaDataUrl,
      assinadoEm: new Date().toISOString(),
    };
    if (pacienteId) salvarRelatorio(salvo);
    setRelatorioSalvo(salvo);
    setAssinado(true);
    setAssinando(false);
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

  if (assinado && relatorioSalvo) {
    return (
      <RelatorioLeituraView
        prontuario={prontuario}
        historico={historico}
        hoje={hoje}
        medicoNome={medicoNome}
        peso={peso}
        temperatura={temperatura}
        frequenciaCardiaca={frequenciaCardiaca}
        tipoRelatorio={tipoRelatorio}
        diagnostico={diagnostico}
        resumoTutor={resumoTutor}
        orientacoes={orientacoes}
        cuidadosPosOp={cuidadosPosOp}
        tempoRecuperacao={tempoRecuperacao}
        jaEmitido={jaEmitido}
        relatorioSalvo={relatorioSalvo}
        onBaixarPdf={() => gerarPdfRelatorio(relatorioSalvo)}
        onFinalizar={async () => {
          if (pacienteId && peso) {
            try { await service.atualizarPesoPaciente(pacienteId, parseFloat(peso)); } catch { /* segue */ }
          }
          navigate(`/medico/prontuario/${pacienteId}`);
        }}
        onVoltar={() => navigate(`/medico/prontuario/${pacienteId}`)}
      />
    );
  }

  const configTipo = CONFIGURACOES_TIPO[tipoRelatorio];

  return (
    <div className="space-y-6">

      {/* ── Cabeçalho ──────────────────────────────────────────────────────── */}
      <div className="flex items-center justify-between">
        <button onClick={() => navigate(`/medico/prontuario/${pacienteId}`)} className="btn-ghost text-sm">
          ← Voltar ao Prontuário
        </button>
        <div className="text-center">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">
            Relatório Clínico Evolutivo
          </p>
          <h1 className="text-xl font-bold text-ink-900">Novo Atendimento</h1>
        </div>
        <div className="w-40" />
      </div>

      {/* ── Banner do paciente ──────────────────────────────────────────────── */}
      <div className="rounded-2xl border border-brand-200 bg-gradient-to-r from-brand-50 to-white p-4 flex items-center gap-4">
        <div className="h-12 w-12 rounded-2xl bg-brand-100 flex items-center justify-center text-2xl shrink-0">
          🐾
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
              Alergias Conhecidas — Atenção antes de prescrever (RN-125)
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

      <form onSubmit={handleAssinar} className="space-y-6">

        {/* ── Tipo de consulta ─────────────────────────────────────────────── */}
        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Tipo de Consulta</h2>
          <p className="mb-4 text-xs text-ink-500">
            Define os campos obrigatórios e a estratégia de validação — padrão Strategy (RN-124)
          </p>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            {(Object.entries(CONFIGURACOES_TIPO) as [TipoRelatorio, ConfiguracaoTipoRelatorio][]).map(
              ([valor, config]) => (
                <button
                  key={valor}
                  type="button"
                  onClick={() => { setTipoRelatorio(valor); setErroAssinatura(null); }}
                  className={
                    "rounded-xl border-2 p-4 text-left transition " +
                    (tipoRelatorio === valor
                      ? "border-brand-400 bg-brand-50"
                      : "border-ink-200 bg-white hover:border-brand-300 hover:bg-brand-50/40")
                  }
                >
                  <span className="text-2xl">{config.icone}</span>
                  <p className={`mt-2 text-sm font-semibold ${tipoRelatorio === valor ? "text-brand-800" : "text-ink-900"}`}>
                    {config.rotulo}
                  </p>
                  <p className="mt-0.5 text-xs leading-relaxed text-ink-500">{config.descricao}</p>
                  {tipoRelatorio === valor && (
                    <p className="mt-2 text-xs font-medium text-brand-600">✓ Selecionado</p>
                  )}
                </button>
              )
            )}
          </div>
        </div>

        {/* ── Sinais vitais ────────────────────────────────────────────────── */}
        <div className="card p-6">
          <h2 className="mb-4 text-base font-semibold text-ink-900">Sinais Vitais do Atendimento</h2>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div>
              <label className="label">Data</label>
              <div className="input cursor-default bg-ink-100/60 text-ink-600">{hoje}</div>
            </div>
            <div>
              <label className="label" htmlFor="peso">Peso (kg)</label>
              <input
                id="peso"
                type="number"
                step="0.1"
                min="0"
                value={peso}
                onChange={(e) => setPeso(e.target.value)}
                className="input"
                placeholder="0.0"
              />
            </div>
            <div>
              <label className="label" htmlFor="temperatura">Temperatura (°C)</label>
              <input
                id="temperatura"
                type="number"
                step="0.1"
                min="0"
                value={temperatura}
                onChange={(e) => setTemperatura(e.target.value)}
                className="input"
                placeholder="38.5"
              />
            </div>
            <div>
              <label className="label" htmlFor="frequenciaCardiaca">FC (bpm)</label>
              <input
                id="frequenciaCardiaca"
                type="number"
                step="1"
                min="0"
                value={frequenciaCardiaca}
                onChange={(e) => setFrequenciaCardiaca(e.target.value)}
                className="input"
                placeholder="80"
              />
            </div>
          </div>
          <div className="mt-4">
            <label className="label">Médico Responsável</label>
            <div className="input cursor-default bg-ink-100/60 text-ink-600">{medicoNome}</div>
          </div>
        </div>

        {/* ── Evolução comparativa ─────────────────────────────────────────── */}
        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Evolução Comparativa</h2>
          <p className="mb-4 text-xs text-ink-500">Histórico dos últimos atendimentos (RN-116)</p>
          {historico.length === 0 ? (
            <p className="text-sm text-ink-500">Primeiro atendimento — sem histórico anterior.</p>
          ) : (
            <div className="overflow-x-auto rounded-xl border border-ink-100">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-ink-100 bg-ink-50/70">
                    {["Data", "Peso (kg)", "Temperatura (°C)"].map((col) => (
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
                  {(peso || temperatura) && (
                    <tr className="bg-brand-50/60">
                      <td className="px-4 py-2.5">
                        <span className="rounded-full bg-brand-100 px-2 py-0.5 text-xs font-semibold text-brand-700">
                          Hoje
                        </span>
                      </td>
                      <td className="px-4 py-2.5 font-semibold text-brand-800">
                        {peso || "—"}
                        {peso && historico[0] && (
                          <Tendencia atual={parseFloat(peso)} anterior={historico[0].pesoKg} />
                        )}
                      </td>
                      <td className="px-4 py-2.5 text-brand-800">
                        {temperatura || "—"}
                        {temperatura && historico[0] && (
                          <Tendencia atual={parseFloat(temperatura)} anterior={historico[0].temperaturaCelsius} />
                        )}
                      </td>
                    </tr>
                  )}
                  {historico.map((r, i) => (
                    <tr key={i} className="hover:bg-ink-50/40">
                      <td className="px-4 py-2.5 text-ink-600">{r.data}</td>
                      <td className="px-4 py-2.5 font-medium text-ink-900">
                        {r.pesoKg}
                        {historico[i + 1] && (
                          <Tendencia atual={r.pesoKg} anterior={historico[i + 1].pesoKg} />
                        )}
                      </td>
                      <td className="px-4 py-2.5 text-ink-800">
                        {r.temperaturaCelsius}
                        {historico[i + 1] && (
                          <Tendencia atual={r.temperaturaCelsius} anterior={historico[i + 1].temperaturaCelsius} />
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* ── Conteúdo clínico ─────────────────────────────────────────────── */}
        <div className="card p-6 space-y-5">
          <h2 className="text-base font-semibold text-ink-900">Conteúdo Clínico</h2>

          {(tipoRelatorio === "ROTINEIRO" || tipoRelatorio === "CIRURGICO") && (
            <div>
              <label className="label" htmlFor="diagnostico">
                Diagnóstico Técnico (CID-Vet ou texto livre)
                <span className="ml-1 text-paw-500">*</span>
              </label>
              <input
                id="diagnostico"
                type="text"
                value={diagnostico}
                onChange={(e) => setDiagnostico(e.target.value)}
                className="input"
                placeholder="Ex: Gastrite aguda ou K29.1"
              />
            </div>
          )}

          <div>
            <label className="label" htmlFor="resumoTutor">
              Resumo para o Tutor{" "}
              <span className="text-xs font-normal text-ink-500">(linguagem acessível)</span>
              <span className="ml-1 text-paw-500">*</span>
            </label>
            <textarea
              id="resumoTutor"
              rows={4}
              value={resumoTutor}
              onChange={(e) => setResumoTutor(e.target.value)}
              className="input resize-none"
              placeholder="Descreva o diagnóstico e o tratamento de forma clara para o tutor..."
            />
          </div>

          <div>
            <label className="label" htmlFor="orientacoes">
              Orientações de Manejo{" "}
              <span className="text-xs font-normal text-ink-400">(opcional)</span>
            </label>
            <textarea
              id="orientacoes"
              rows={3}
              value={orientacoes}
              onChange={(e) => setOrientacoes(e.target.value)}
              className="input resize-none"
              placeholder="Instruções detalhadas para o tratamento em casa..."
            />
          </div>
        </div>

        {/* ── Campos cirúrgicos ────────────────────────────────────────────── */}
        {tipoRelatorio === "CIRURGICO" && (
          <div className="card border-l-4 border-brand-400 p-6 space-y-4">
            <div className="flex items-center gap-2">
              <span className="text-lg text-brand-600">🔬</span>
              <h2 className="text-base font-semibold text-ink-900">
                Campos Cirúrgicos Obrigatórios (RN-124)
              </h2>
            </div>
            <div>
              <label className="label" htmlFor="cuidadosPosOp">
                Cuidados Pós-Operatórios <span className="text-paw-500">*</span>
              </label>
              <textarea
                id="cuidadosPosOp"
                rows={3}
                value={cuidadosPosOp}
                onChange={(e) => setCuidadosPosOp(e.target.value)}
                className="input resize-none"
                placeholder="Ex: Manter colar elizabetano. Não molhar a incisão por 10 dias..."
              />
            </div>
            <div>
              <label className="label" htmlFor="tempoRecuperacao">
                Tempo de Recuperação Estimado <span className="text-paw-500">*</span>
              </label>
              <input
                id="tempoRecuperacao"
                type="text"
                value={tempoRecuperacao}
                onChange={(e) => setTempoRecuperacao(e.target.value)}
                className="input"
                placeholder="Ex: 10 a 14 dias"
              />
            </div>
          </div>
        )}

        {/* ── Aplicar Vacina (apenas consulta preventiva) ──────────────────── */}
        {tipoRelatorio === "PREVENTIVO" && (
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

            {vacinasAplicadas.length > 0 && (
              <ul className="mt-3 space-y-1.5">
                {vacinasAplicadas.map((v, i) => (
                  <li key={i} className="flex items-center gap-2 rounded-xl bg-brand-50 px-3 py-2 text-sm text-brand-800">
                    <span>✅</span> {v}
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        {/* ── Anexos clínicos (não exibidos em consulta preventiva) ─────────── */}
        {tipoRelatorio !== "PREVENTIVO" && (
        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Anexos Clínicos</h2>
          <p className="mb-3 text-xs text-ink-500">
            Fotos de lesões, exames ou laudos em PDF · máximo 4 arquivos (RN-119)
          </p>
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            disabled={arquivosSelecionados.length >= 4}
            className={
              "w-full rounded-xl border-2 border-dashed px-4 py-5 text-center transition " +
              (arquivosSelecionados.length >= 4
                ? "cursor-not-allowed border-ink-200 bg-ink-50"
                : "cursor-pointer border-ink-300 bg-ink-50/40 hover:border-brand-400 hover:bg-brand-50/30")
            }
          >
            <span className="block text-2xl mb-1">📎</span>
            <span className="block text-sm font-medium text-ink-600">
              {arquivosSelecionados.length >= 4
                ? "Limite de 4 arquivos atingido"
                : "Clique para escolher arquivos"}
            </span>
            <span className="block mt-0.5 text-xs text-ink-400">
              {arquivosSelecionados.length === 0
                ? "Imagens (JPG, PNG) ou PDF"
                : `${arquivosSelecionados.length}/4 arquivo(s) selecionado(s)`}
            </span>
          </button>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            accept="image/*,.pdf"
            className="hidden"
            onChange={handleArquivos}
          />
          {arquivosSelecionados.length > 0 && (
            <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-4">
              {arquivosSelecionados.map((f, i) => (
                <AnexoPreview key={i} file={f} onRemover={() => removerArquivo(i)} />
              ))}
            </div>
          )}
        </div>
        )}

        {/* ── Medicamentos prescritos (não exibidos em consulta preventiva) ── */}
        {tipoRelatorio !== "PREVENTIVO" && (
        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Medicamentos Prescritos</h2>
          <p className="mb-3 text-xs text-ink-500">
            Incluídos automaticamente com base na prescrição registrada
          </p>
          <ul className="space-y-2">
            {MEDICAMENTOS_STUB.map((m, i) => (
              <li
                key={i}
                className="flex items-start gap-3 rounded-xl bg-ink-50/60 px-3 py-2.5 text-sm text-ink-700"
              >
                <span className="mt-0.5 shrink-0 text-brand-500">💊</span>
                {m}
              </li>
            ))}
          </ul>
        </div>
        )}

        {/* ── Assinatura digital do médico (desenho com o mouse) ────────────── */}
        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Assinatura Digital do Médico</h2>
          <p className="mb-3 text-xs text-ink-500">
            Assine no quadro abaixo com o mouse (ou o dedo). A assinatura consta no PDF do relatório.
          </p>
          <SignaturePad ref={assinaturaRef} onChange={setAssinaturaVazia} />
        </div>

        {/* ── Rodapé: erro + assinar ────────────────────────────────────────── */}
        {erroAssinatura && (
          <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
            <span className="mr-2 font-bold">⚠</span>
            {erroAssinatura}
          </div>
        )}

        <div className="rounded-2xl border border-dashed border-ink-300/70 bg-white/90 p-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-medium text-ink-700">
              Tipo selecionado:{" "}
              <span className="font-semibold text-brand-700">
                {configTipo.icone} {configTipo.rotulo}
              </span>
            </p>
            <p className="mt-0.5 text-xs text-ink-500">
              Após assinar, o relatório torna-se somente leitura e é publicado no portal do tutor (RN-120).
            </p>
          </div>
          <button
            type="submit"
            disabled={assinando || assinaturaVazia}
            className="btn-primary shrink-0 sm:w-auto"
            title={assinaturaVazia ? "Desenhe sua assinatura para habilitar" : undefined}
          >
            {assinando ? "Assinando digitalmente..." : "✍ Assinar e Publicar"}
          </button>
        </div>

      </form>
    </div>
  );
}

// ── Prévia de anexo (miniatura de imagem ou ícone de PDF) ──────────────────────

function AnexoPreview({ file, onRemover }: { file: File; onRemover: () => void }) {
  const [url, setUrl] = useState<string | null>(null);

  useEffect(() => {
    if (!file.type.startsWith("image/")) return;
    const objectUrl = URL.createObjectURL(file);
    setUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [file]);

  return (
    <div className="group relative overflow-hidden rounded-xl border border-ink-200 bg-ink-50">
      <div className="flex h-24 items-center justify-center">
        {url ? (
          <img src={url} alt={file.name} className="h-full w-full object-cover" />
        ) : (
          <div className="flex flex-col items-center text-ink-400">
            <span className="text-2xl">📄</span>
            <span className="text-[10px]">PDF</span>
          </div>
        )}
      </div>
      <div className="flex items-center justify-between gap-1 px-2 py-1">
        <span className="truncate text-[10px] text-ink-600">{file.name}</span>
        <button
          type="button"
          onClick={onRemover}
          className="shrink-0 rounded p-0.5 text-ink-400 transition hover:bg-paw-50 hover:text-paw-500"
          aria-label={`Remover ${file.name}`}
        >
          ✕
        </button>
      </div>
    </div>
  );
}

// ── Tendência comparativa ──────────────────────────────────────────────────────

function Tendencia({ atual, anterior }: { atual: number; anterior: number }) {
  if (atual > anterior)
    return <span className="ml-1 text-xs font-bold text-amber-500" title="Aumento">↑</span>;
  if (atual < anterior)
    return <span className="ml-1 text-xs font-bold text-brand-500" title="Redução">↓</span>;
  return <span className="ml-1 text-xs text-ink-400" title="Estável">→</span>;
}

// ── Visualização somente leitura (após assinatura) ─────────────────────────────

function RelatorioLeituraView({
  prontuario, historico, hoje, medicoNome, peso, temperatura, frequenciaCardiaca,
  tipoRelatorio, diagnostico, resumoTutor, orientacoes,
  cuidadosPosOp, tempoRecuperacao, jaEmitido, relatorioSalvo,
  onBaixarPdf, onFinalizar, onVoltar,
}: {
  prontuario: ProntuarioDTO;
  historico: RegistroHistoricoDTO[];
  hoje: string;
  medicoNome: string;
  peso: string;
  temperatura: string;
  frequenciaCardiaca: string;
  tipoRelatorio: TipoRelatorio;
  diagnostico: string;
  resumoTutor: string;
  orientacoes: string;
  cuidadosPosOp: string;
  tempoRecuperacao: string;
  jaEmitido: boolean;
  relatorioSalvo: RelatorioSalvo;
  onBaixarPdf: () => void;
  onFinalizar: () => Promise<void>;
  onVoltar: () => void;
}) {
  const configTipo = CONFIGURACOES_TIPO[tipoRelatorio];
  const [finalizando, setFinalizando] = useState(false);

  async function handleFinalizar() {
    setFinalizando(true);
    await onFinalizar();
  }

  return (
    <div className="space-y-6">

      <div className="flex items-center justify-between">
        <button onClick={onVoltar} className="btn-ghost text-sm">
          ← Voltar ao Prontuário
        </button>
        <div className="text-center">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">
            Relatório Clínico Evolutivo
          </p>
          <h1 className="text-xl font-bold text-ink-900">Documento Assinado</h1>
        </div>
        <div className="w-40" />
      </div>

      {/* Banner de assinatura */}
      <div className="rounded-2xl border-2 border-brand-300 bg-brand-50 p-4 flex items-center gap-4">
        <span className="text-3xl">✅</span>
        <div className="flex-1 min-w-0">
          <p className="font-bold text-brand-800">
            {jaEmitido ? "Relatório já emitido para este atendimento" : "Relatório assinado digitalmente"}
          </p>
          <p className="mt-0.5 text-xs text-brand-700">
            {jaEmitido
              ? `Emitido em ${relatorioSalvo.data} · Documento somente leitura (RN-120)`
              : `Publicado no portal do Tutor em ${hoje} · Documento somente leitura (RN-120)`}
          </p>
        </div>
        <button
          type="button"
          className="ml-auto shrink-0 rounded-xl border border-brand-400 bg-white px-4 py-2 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
          onClick={onBaixarPdf}
        >
          ⬇ Baixar PDF
        </button>
      </div>

      {/* Dados do atendimento */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Dados do Atendimento</h2>
        <div className="grid grid-cols-2 gap-x-6 gap-y-4 sm:grid-cols-3">
          <CampoLeitura rotulo="Paciente" valor={prontuario.nomePet} />
          <CampoLeitura rotulo="Tutor" valor={prontuario.nomeTutor} />
          <CampoLeitura rotulo="Espécie / Raça" valor={`${prontuario.especie} · ${prontuario.raca}`} />
          <CampoLeitura rotulo="Tipo de Consulta" valor={`${configTipo.icone} ${configTipo.rotulo}`} />
          <CampoLeitura rotulo="Data" valor={hoje} />
          <CampoLeitura rotulo="Médico Responsável" valor={medicoNome} />
          {peso && <CampoLeitura rotulo="Peso" valor={`${peso} kg`} />}
          {temperatura && <CampoLeitura rotulo="Temperatura" valor={`${temperatura} °C`} />}
          {frequenciaCardiaca && <CampoLeitura rotulo="Freq. Cardíaca" valor={`${frequenciaCardiaca} bpm`} />}
        </div>
      </div>

      {/* Evolução comparativa */}
      {historico.length > 0 && (
        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">Evolução Comparativa</h2>
          <div className="overflow-x-auto rounded-xl border border-ink-100">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-ink-100 bg-ink-50/70">
                  {["Data", "Peso (kg)", "Temperatura (°C)"].map((col) => (
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
                {(peso || temperatura) && (
                  <tr className="bg-brand-50/60">
                    <td className="px-4 py-2.5">
                      <span className="rounded-full bg-brand-100 px-2 py-0.5 text-xs font-semibold text-brand-700">
                        Hoje
                      </span>
                    </td>
                    <td className="px-4 py-2.5 font-semibold text-brand-800">
                      {peso || "—"}
                      {peso && historico[0] && (
                        <Tendencia atual={parseFloat(peso)} anterior={historico[0].pesoKg} />
                      )}
                    </td>
                    <td className="px-4 py-2.5 text-brand-800">
                      {temperatura || "—"}
                      {temperatura && historico[0] && (
                        <Tendencia atual={parseFloat(temperatura)} anterior={historico[0].temperaturaCelsius} />
                      )}
                    </td>
                  </tr>
                )}
                {historico.map((r, i) => (
                  <tr key={i}>
                    <td className="px-4 py-2.5 text-ink-600">{r.data}</td>
                    <td className="px-4 py-2.5 font-medium text-ink-900">
                      {r.pesoKg}
                      {historico[i + 1] && (
                        <Tendencia atual={r.pesoKg} anterior={historico[i + 1].pesoKg} />
                      )}
                    </td>
                    <td className="px-4 py-2.5 text-ink-800">
                      {r.temperaturaCelsius}
                      {historico[i + 1] && (
                        <Tendencia atual={r.temperaturaCelsius} anterior={historico[i + 1].temperaturaCelsius} />
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Conteúdo clínico */}
      <div className="card p-6 space-y-5">
        <h2 className="text-base font-semibold text-ink-900">Conteúdo Clínico</h2>
        {diagnostico && <CampoLeitura rotulo="Diagnóstico Técnico" valor={diagnostico} multiline />}
        {resumoTutor && (
          <CampoLeitura rotulo="Resumo para o Tutor" valor={resumoTutor} multiline destaque />
        )}
        {orientacoes && (
          <CampoLeitura rotulo="Orientações de Manejo" valor={orientacoes} multiline />
        )}
        {tipoRelatorio === "CIRURGICO" && (
          <>
            <CampoLeitura rotulo="Cuidados Pós-Operatórios" valor={cuidadosPosOp} multiline />
            <CampoLeitura rotulo="Tempo de Recuperação Estimado" valor={tempoRecuperacao} />
          </>
        )}
      </div>

      {/* Anexos */}
      {relatorioSalvo.anexos.length > 0 && (
        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">Anexos Clínicos</h2>
          <ul className="space-y-1.5">
            {relatorioSalvo.anexos.map((nome, i) => (
              <li key={i} className="flex items-center gap-2 text-sm text-brand-700">
                <span>📎</span>
                {nome}
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Medicamentos */}
      {relatorioSalvo.medicamentos.length > 0 && (
        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">Medicamentos Prescritos</h2>
          <ul className="space-y-2">
            {relatorioSalvo.medicamentos.map((m, i) => (
              <li
                key={i}
                className="flex items-start gap-3 rounded-xl bg-ink-50/60 px-3 py-2.5 text-sm text-ink-700"
              >
                <span className="mt-0.5 shrink-0 text-brand-500">💊</span>
                {m}
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Assinatura do médico */}
      {relatorioSalvo.assinaturaDataUrl && (
        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">Assinatura do Médico</h2>
          <div className="flex flex-col items-center">
            <img
              src={relatorioSalvo.assinaturaDataUrl}
              alt="Assinatura do médico"
              className="h-24 object-contain"
            />
            <div className="mt-1 w-64 border-t border-ink-400 pt-1 text-center text-xs text-ink-600">
              {medicoNome} — Médico(a) Veterinário(a)
            </div>
          </div>
        </div>
      )}

      {/* Rodapé: finalizar prontuário */}
      <div className="rounded-2xl border-2 border-brand-200 bg-brand-50/60 p-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-ink-800">Concluir atendimento</p>
          <p className="mt-0.5 text-xs text-ink-500">
            Finalizar o prontuário atualiza o último peso registrado do paciente
            {peso ? <> para <strong>{peso} kg</strong></> : ""} e encerra o atendimento.
          </p>
        </div>
        <div className="flex shrink-0 gap-3">
          <button
            type="button"
            onClick={onBaixarPdf}
            className="rounded-xl border border-brand-400 bg-white px-4 py-2.5 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
          >
            ⬇ Baixar PDF
          </button>
          <button
            type="button"
            onClick={handleFinalizar}
            disabled={finalizando}
            className="btn-primary sm:w-auto"
          >
            {finalizando ? "Finalizando..." : "✓ Finalizar Prontuário"}
          </button>
        </div>
      </div>

      {/* Canal de comunicação */}
      <div className="rounded-2xl border border-dashed border-ink-300/70 bg-white/90 p-5 flex flex-wrap items-center justify-between gap-4">
        <p className="text-xs text-ink-500">
          Dúvidas sobre o tratamento? O tutor pode entrar em contato com a clínica.
        </p>
        <button
          type="button"
          className="rounded-xl border border-paw-300 bg-paw-50 px-4 py-2 text-sm font-medium text-paw-700 transition hover:bg-paw-100"
          onClick={() => alert("Canal de comunicação da clínica (RN-121) — integração em desenvolvimento.")}
        >
          Dúvida Urgente
        </button>
      </div>

    </div>
  );
}

// ── Subcomponentes ─────────────────────────────────────────────────────────────

function CampoLeitura({
  rotulo, valor, multiline = false, destaque = false,
}: {
  rotulo: string;
  valor: string;
  multiline?: boolean;
  destaque?: boolean;
}) {
  return (
    <div>
      <dt className="mb-1 text-xs font-medium text-ink-500">{rotulo}</dt>
      <dd
        className={
          "text-sm " +
          (destaque
            ? "rounded-xl bg-brand-50 p-3 text-brand-800 ring-1 ring-brand-100"
            : "text-ink-900") +
          (multiline ? " whitespace-pre-wrap" : "")
        }
      >
        {valor}
      </dd>
    </div>
  );
}
