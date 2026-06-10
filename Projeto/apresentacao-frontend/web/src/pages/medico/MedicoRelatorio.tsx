import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  HISTORICO_STUB,
  MEDICAMENTOS_STUB,
  type ProntuarioDTO,
  type RegistroHistoricoDTO,
  type TipoRelatorio,
} from "./medicoService";

const TIPOS_RELATORIO: { valor: TipoRelatorio; rotulo: string }[] = [
  { valor: "ROTINEIRO", rotulo: "Consulta Rotineira" },
  { valor: "CIRURGICO", rotulo: "Procedimento Cirúrgico" },
  { valor: "PREVENTIVO", rotulo: "Consulta Preventiva" },
];

export function MedicoRelatorio() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const navigate = useNavigate();
  const { apiFetch } = useAuth();
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
  const [diagnostico, setDiagnostico] = useState("");
  const [resumoTutor, setResumoTutor] = useState("");
  const [orientacoes, setOrientacoes] = useState("");
  const [cuidadosPosOp, setCuidadosPosOp] = useState("");
  const [tempoRecuperacao, setTempoRecuperacao] = useState("");
  const [arquivosSelecionados, setArquivosSelecionados] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const hoje = new Date().toLocaleDateString("pt-BR");
  const medicoNome = "Dr. Carlos Silva";

  useEffect(() => {
    if (!pacienteId) return;
    Promise.all([
      service.buscarProntuario(pacienteId),
      Promise.resolve(HISTORICO_STUB),
    ])
      .then(([p, h]) => { setProntuario(p); setHistorico(h); })
      .finally(() => setCarregando(false));
  }, [service, pacienteId]);

  function handleArquivos(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    setArquivosSelecionados((prev) => {
      const combined = [...prev, ...files];
      return combined.slice(0, 4);
    });
  }

  function removerArquivo(index: number) {
    setArquivosSelecionados((prev) => prev.filter((_, i) => i !== index));
  }

  function validarParaAssinatura(): string | null {
    if (tipoRelatorio === "ROTINEIRO" || tipoRelatorio === "CIRURGICO") {
      if (!diagnostico.trim()) return "O diagnóstico técnico é obrigatório.";
    }
    if (!resumoTutor.trim()) return "O resumo para o tutor é obrigatório.";
    if (tipoRelatorio === "CIRURGICO") {
      if (!cuidadosPosOp.trim()) return "Os cuidados pós-operatórios são obrigatórios para relatório cirúrgico.";
      if (!tempoRecuperacao.trim()) return "O tempo de recuperação estimado é obrigatório para relatório cirúrgico.";
    }
    return null;
  }

  async function handleAssinar(e: React.FormEvent) {
    e.preventDefault();
    const erro = validarParaAssinatura();
    if (erro) { setErroAssinatura(erro); return; }
    setAssinando(true);
    setErroAssinatura(null);
    await new Promise((r) => setTimeout(r, 800));
    setAssinado(true);
    setAssinando(false);
  }

  if (carregando) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-64 animate-pulse rounded-xl bg-ink-100" />
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

  if (assinado) {
    return (
      <RelatorioLeituraView
        prontuario={prontuario}
        historico={historico}
        hoje={hoje}
        medicoNome={medicoNome}
        peso={peso}
        temperatura={temperatura}
        tipoRelatorio={tipoRelatorio}
        diagnostico={diagnostico}
        resumoTutor={resumoTutor}
        orientacoes={orientacoes}
        cuidadosPosOp={cuidadosPosOp}
        tempoRecuperacao={tempoRecuperacao}
        arquivos={arquivosSelecionados}
        onVoltar={() => navigate(`/medico/prontuario/${pacienteId}`)}
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(`/medico/prontuario/${pacienteId}`)}
          className="btn-ghost text-sm"
        >
          ← Voltar ao Prontuário
        </button>
        <h1 className="text-xl font-bold text-ink-900">Relatório Clínico Evolutivo</h1>
        <div className="w-40" />
      </div>

      {prontuario.alergias.length > 0 && (
        <div role="alert" className="rounded-2xl border-2 border-red-300 bg-red-50 p-4">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-lg">⚠️</span>
            <h2 className="font-bold text-red-800 text-sm">Alergias Conhecidas (RN-125)</h2>
          </div>
          <p className="text-sm text-red-700">
            {prontuario.alergias.map((a, i) => (
              <span key={i}>{i > 0 && <span className="mx-2 text-red-400">|</span>}• {a}</span>
            ))}
          </p>
        </div>
      )}

      <form onSubmit={handleAssinar} className="space-y-6">
        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Tipo de Consulta</h2>
          <p className="mb-4 text-xs text-ink-500">
            Define os campos obrigatórios para assinatura (padrão Strategy — RN-124)
          </p>
          <div className="flex flex-wrap gap-2">
            {TIPOS_RELATORIO.map((t) => (
              <button
                key={t.valor}
                type="button"
                onClick={() => setTipoRelatorio(t.valor)}
                className={
                  "rounded-xl border px-4 py-2 text-sm font-medium transition " +
                  (tipoRelatorio === t.valor
                    ? "border-brand-500 bg-brand-50 text-brand-700"
                    : "border-ink-300 bg-white text-ink-700 hover:border-brand-400 hover:bg-brand-50/50")
                }
              >
                {t.rotulo}
              </button>
            ))}
          </div>
        </div>

        <div className="card p-6">
          <h2 className="mb-4 text-base font-semibold text-ink-900">Dados Automáticos do Atendimento</h2>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div>
              <label className="label">Data</label>
              <div className="input bg-ink-100/60 text-ink-600 cursor-default">{hoje}</div>
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
              <label className="label">Médico Responsável</label>
              <div className="input bg-ink-100/60 text-ink-600 cursor-default">{medicoNome}</div>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <h2 className="mb-1 text-base font-semibold text-ink-900">Evolução Comparativa</h2>
          <p className="mb-4 text-xs text-ink-500">Gráfico de Variação — Últimos 3 Atendimentos (RN-116)</p>
          {historico.length === 0 ? (
            <p className="text-sm text-ink-500">Primeiro atendimento — sem histórico anterior.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-ink-100 bg-ink-50/50">
                    {["Data", "Peso (kg)", "Temperatura (°C)"].map((h) => (
                      <th key={h} className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100">
                  {historico.map((r, i) => (
                    <tr key={i} className="hover:bg-ink-50/40">
                      <td className="px-4 py-2 text-ink-800">{r.data}</td>
                      <td className="px-4 py-2 font-medium text-ink-900">{r.pesoKg}</td>
                      <td className="px-4 py-2 text-ink-800">{r.temperaturaCelsius}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="card p-6 space-y-4">
          <div>
            <label className="label" htmlFor="diagnostico">
              Diagnóstico (CID-Vet ou texto livre)
              {(tipoRelatorio === "ROTINEIRO" || tipoRelatorio === "CIRURGICO") && (
                <span className="ml-1 text-paw-500">*</span>
              )}
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

          <div>
            <label className="label" htmlFor="resumoTutor">
              Resumo para o Tutor <span className="text-xs font-normal text-ink-500">(linguagem acessível)</span>
              <span className="ml-1 text-paw-500">*</span>
            </label>
            <textarea
              id="resumoTutor"
              rows={4}
              value={resumoTutor}
              onChange={(e) => setResumoTutor(e.target.value)}
              className="input resize-none"
              placeholder="Descreva o diagnóstico e tratamento de forma clara para o Tutor..."
            />
          </div>

          <div>
            <label className="label" htmlFor="orientacoes">Orientações de Manejo</label>
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

        {tipoRelatorio === "CIRURGICO" && (
          <div className="card border-l-4 border-brand-400 p-6 space-y-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-brand-600 text-lg">🔬</span>
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

        <div className="card p-6">
          <label className="label">Upload de Fotos ou Laudos PDF (máx. 4)</label>
          <div
            className="mt-1 flex cursor-pointer items-center gap-3 rounded-xl border border-dashed border-ink-300 bg-ink-50/40 px-4 py-3 transition hover:border-brand-400 hover:bg-brand-50/30"
            onClick={() => fileInputRef.current?.click()}
          >
            <span className="text-ink-400 text-sm">Escolher arquivos</span>
            <span className="text-xs text-ink-400">
              {arquivosSelecionados.length === 0
                ? "Nenhum arquivo escolhido"
                : `${arquivosSelecionados.length} arquivo(s)`}
            </span>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            accept="image/*,.pdf"
            className="hidden"
            onChange={handleArquivos}
          />
          {arquivosSelecionados.length > 0 && (
            <ul className="mt-3 space-y-1">
              {arquivosSelecionados.map((f, i) => (
                <li key={i} className="flex items-center justify-between rounded-lg bg-ink-50 px-3 py-1.5 text-xs">
                  <span className="text-ink-700 truncate">{f.name}</span>
                  <button
                    type="button"
                    onClick={() => removerArquivo(i)}
                    className="ml-2 shrink-0 text-ink-400 hover:text-paw-500 transition"
                  >
                    ✕
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">
            Medicamentos Prescritos (incluídos automaticamente)
          </h2>
          <ul className="space-y-1.5">
            {MEDICAMENTOS_STUB.map((m, i) => (
              <li key={i} className="flex items-start gap-2 text-sm text-ink-700">
                <span className="mt-0.5 text-brand-500">•</span>
                {m}
              </li>
            ))}
          </ul>
        </div>

        {erroAssinatura && (
          <div
            role="alert"
            className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
          >
            {erroAssinatura}
          </div>
        )}

        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-xs text-ink-500">
            Após assinar, o relatório torna-se somente leitura (RN-120).
          </p>
          <button
            type="submit"
            disabled={assinando}
            className="btn-primary sm:w-auto"
          >
            {assinando ? "Assinando..." : "Assinar Digitalmente e Publicar"}
          </button>
        </div>
      </form>
    </div>
  );
}

// ── View de Leitura (após assinatura) ─────────────────────────────────────────

function RelatorioLeituraView({
  prontuario, historico, hoje, medicoNome, peso, temperatura,
  tipoRelatorio, diagnostico, resumoTutor, orientacoes,
  cuidadosPosOp, tempoRecuperacao, arquivos, onVoltar,
}: {
  prontuario: ProntuarioDTO;
  historico: RegistroHistoricoDTO[];
  hoje: string;
  medicoNome: string;
  peso: string;
  temperatura: string;
  tipoRelatorio: TipoRelatorio;
  diagnostico: string;
  resumoTutor: string;
  orientacoes: string;
  cuidadosPosOp: string;
  tempoRecuperacao: string;
  arquivos: File[];
  onVoltar: () => void;
}) {
  const tipoRotulo = TIPOS_RELATORIO.find((t) => t.valor === tipoRelatorio)?.rotulo ?? tipoRelatorio;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <button onClick={onVoltar} className="btn-ghost text-sm">
          ← Voltar ao Prontuário
        </button>
        <h1 className="text-xl font-bold text-ink-900">Relatório Clínico Evolutivo</h1>
        <div className="w-40" />
      </div>

      <div className="rounded-2xl border-2 border-brand-300 bg-brand-50 p-4 flex items-center gap-3">
        <span className="text-2xl">✅</span>
        <div>
          <p className="font-semibold text-brand-800">Relatório assinado digitalmente</p>
          <p className="text-xs text-brand-700">
            Publicado no portal do Tutor em {hoje}. Documento somente leitura.
          </p>
        </div>
        <button
          type="button"
          className="ml-auto rounded-xl border border-brand-400 bg-white px-4 py-2 text-sm font-medium text-brand-700 hover:bg-brand-50 transition"
          onClick={() => alert("Exportação PDF com timbre da clínica e CRMV (RN-122) — funcionalidade em desenvolvimento.")}
        >
          Exportar PDF
        </button>
      </div>

      <div className="card p-6 space-y-3">
        <CampoLeitura rotulo="Paciente" valor={`${prontuario.nomePet} — ${prontuario.nomeTutor}`} />
        <CampoLeitura rotulo="Tipo de Consulta" valor={tipoRotulo} />
        <CampoLeitura rotulo="Data" valor={hoje} />
        <CampoLeitura rotulo="Médico" valor={medicoNome} />
        {peso && <CampoLeitura rotulo="Peso" valor={`${peso} kg`} />}
        {temperatura && <CampoLeitura rotulo="Temperatura" valor={`${temperatura} °C`} />}
      </div>

      {historico.length > 0 && (
        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">Evolução Comparativa</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-ink-100 bg-ink-50/50">
                  {["Data", "Peso (kg)", "Temperatura (°C)"].map((h) => (
                    <th key={h} className="px-4 py-2 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {historico.map((r, i) => (
                  <tr key={i}>
                    <td className="px-4 py-2 text-ink-800">{r.data}</td>
                    <td className="px-4 py-2 font-medium text-ink-900">{r.pesoKg}</td>
                    <td className="px-4 py-2 text-ink-800">{r.temperaturaCelsius}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="card p-6 space-y-4">
        {diagnostico && <CampoLeitura rotulo="Diagnóstico" valor={diagnostico} multiline />}
        {resumoTutor && <CampoLeitura rotulo="Resumo para o Tutor" valor={resumoTutor} multiline destaque />}
        {orientacoes && <CampoLeitura rotulo="Orientações de Manejo" valor={orientacoes} multiline />}
        {tipoRelatorio === "CIRURGICO" && (
          <>
            <CampoLeitura rotulo="Cuidados Pós-Operatórios" valor={cuidadosPosOp} multiline />
            <CampoLeitura rotulo="Tempo de Recuperação Estimado" valor={tempoRecuperacao} />
          </>
        )}
      </div>

      {arquivos.length > 0 && (
        <div className="card p-6">
          <h2 className="mb-3 text-base font-semibold text-ink-900">Anexos</h2>
          <ul className="space-y-1">
            {arquivos.map((f, i) => (
              <li key={i} className="text-sm text-brand-700">📎 {f.name}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="card p-6">
        <h2 className="mb-3 text-base font-semibold text-ink-900">Medicamentos Prescritos</h2>
        <ul className="space-y-1.5">
          {MEDICAMENTOS_STUB.map((m, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-ink-700">
              <span className="mt-0.5 text-brand-500">•</span>
              {m}
            </li>
          ))}
        </ul>
      </div>

      <div className="rounded-2xl border border-dashed border-ink-300/70 bg-white/90 p-4 flex flex-wrap items-center justify-between gap-4">
        <p className="text-xs text-ink-500">
          Dúvidas sobre o tratamento? Entre em contato com a clínica.
        </p>
        <button
          type="button"
          className="rounded-xl border border-paw-300 bg-paw-50 px-4 py-2 text-sm font-medium text-paw-700 hover:bg-paw-100 transition"
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
