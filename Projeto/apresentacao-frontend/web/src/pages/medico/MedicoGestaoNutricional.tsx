import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { criarMedicoService } from "./medicoService";
import { SignaturePad, type SignaturePadHandle } from "./SignaturePad";
import {
  criarNutricaoMedicoService,
  fmt,
  ROTULOS_COMORBIDADE,
  ROTULOS_NIVEL_ATIVIDADE,
  ROTULOS_TIPO_CRONOGRAMA,
  type Comorbidade,
  type CronogramaDTO,
  type DiaTransicaoDTO,
  type EvolucaoNutricionalDTO,
  type HistoricoEvolutivoDTO,
  type NivelAtividade,
  type ParametrosDTO,
  type PlanoNutricionalDTO,
  type PreviewNEMDTO,
  type RacaoDTO,
  type RacaoRecomendadaDTO,
  type TipoCronograma,
} from "./nutricaoService";

// ── Defaults ─────────────────────────────────────────────────────────────────

const PARAMETROS_INICIAIS: ParametrosDTO = {
  pesoAtualKg: 0,
  pesoIdealKg: 0,
  nivelAtividade: "MODERADAMENTE_ATIVO",
  comorbidade: "NENHUMA",
  densidadeCaloricaKcalPorKg: 3500,
};

const CRONOGRAMAS_PADRAO: Record<Exclude<TipoCronograma, "PERSONALIZADO">, DiaTransicaoDTO[]> = {
  PADRAO_7_DIAS: [
    { faixaDias: "Dias 1-2", percentualRacaoAtual: 75, percentualRacaoNova: 25 },
    { faixaDias: "Dias 3-4", percentualRacaoAtual: 50, percentualRacaoNova: 50 },
    { faixaDias: "Dias 5-6", percentualRacaoAtual: 25, percentualRacaoNova: 75 },
    { faixaDias: "Dia 7",     percentualRacaoAtual: 0,  percentualRacaoNova: 100 },
  ],
  PADRAO_10_DIAS: [
    { faixaDias: "Dias 1-3",  percentualRacaoAtual: 75, percentualRacaoNova: 25 },
    { faixaDias: "Dias 4-6",  percentualRacaoAtual: 50, percentualRacaoNova: 50 },
    { faixaDias: "Dias 7-9",  percentualRacaoAtual: 25, percentualRacaoNova: 75 },
    { faixaDias: "Dia 10",    percentualRacaoAtual: 0,  percentualRacaoNova: 100 },
  ],
  PADRAO_14_DIAS: [
    { faixaDias: "Dias 1-4",   percentualRacaoAtual: 75, percentualRacaoNova: 25 },
    { faixaDias: "Dias 5-8",   percentualRacaoAtual: 50, percentualRacaoNova: 50 },
    { faixaDias: "Dias 9-12",  percentualRacaoAtual: 25, percentualRacaoNova: 75 },
    { faixaDias: "Dias 13-14", percentualRacaoAtual: 0,  percentualRacaoNova: 100 },
  ],
};

// ── Componente principal ─────────────────────────────────────────────────────

export function MedicoGestaoNutricional() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const navigate = useNavigate();
  const { apiFetch } = useAuth();

  const medicoService = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);
  const nutricaoService = useMemo(() => criarNutricaoMedicoService(apiFetch), [apiFetch]);

  // Cabeçalho do paciente (vem do prontuário existente — read-only aqui)
  const [nomePet, setNomePet] = useState<string>("…");
  const [tutorId, setTutorId] = useState<string>("");
  const [nomeTutor, setNomeTutor] = useState<string>("");
  const [idadeAnos, setIdadeAnos] = useState<number>(0);

  // Estado do formulário
  const [parametros, setParametros] = useState<ParametrosDTO>(PARAMETROS_INICIAIS);
  const [tipoCronograma, setTipoCronograma] = useState<TipoCronograma>("PADRAO_7_DIAS");
  const [diasCronograma, setDiasCronograma] = useState<DiaTransicaoDTO[]>(CRONOGRAMAS_PADRAO.PADRAO_7_DIAS);
  const [observacoes, setObservacoes] = useState<string[]>([]);
  const [novaObservacao, setNovaObservacao] = useState("");
  const [racaoSelecionadaId, setRacaoSelecionadaId] = useState<string | null>(null);
  const [justificativaDivergencia, setJustificativaDivergencia] = useState<string>("");

  // Catálogo + recomendações + histórico
  const [catalogo, setCatalogo] = useState<RacaoDTO[]>([]);
  const [recomendacoes, setRecomendacoes] = useState<RacaoRecomendadaDTO[]>([]);
  const [historico, setHistorico] = useState<HistoricoEvolutivoDTO | null>(null);

  // Estado do plano em backend
  const [planoRascunho, setPlanoRascunho] = useState<PlanoNutricionalDTO | null>(null);
  const [planoFinalizado, setPlanoFinalizado] = useState<PlanoNutricionalDTO | null>(null);

  // Preview ao vivo
  const [preview, setPreview] = useState<PreviewNEMDTO | null>(null);
  const [erroPreview, setErroPreview] = useState<string | null>(null);

  // Modal de assinatura
  const [mostrandoAssinatura, setMostrandoAssinatura] = useState(false);
  const [assinaturaVazia, setAssinaturaVazia] = useState(true);
  const padRef = useRef<SignaturePadHandle>(null);

  // Feedback geral
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [finalizando, setFinalizando] = useState(false);

  // Carregamento inicial: prontuário (nome do pet/tutor) + rascunho existente
  // + catálogo de rações + histórico de planos
  useEffect(() => {
    if (!pacienteId) return;
    medicoService.buscarProntuario(pacienteId)
      .then(p => {
        setNomePet(p.nomePet);
        setNomeTutor(p.nomeTutor);
        setIdadeAnos(p.idadeAnos);
        setParametros(prev =>
          prev.pesoAtualKg === 0 && p.pesoKg > 0 ? { ...prev, pesoAtualKg: p.pesoKg } : prev
        );
      })
      .catch(() => { /* mantém placeholder */ });

    nutricaoService.buscarRascunho(pacienteId)
      .then(r => {
        if (!r) return;
        setPlanoRascunho(r);
        setTutorId(r.tutorId);
        setParametros(r.parametros);
        setTipoCronograma(r.cronograma.tipo);
        setDiasCronograma(r.cronograma.dias);
        setObservacoes(r.observacoes);
        setRacaoSelecionadaId(r.racaoId);
        setJustificativaDivergencia(r.justificativaDivergencia ?? "");
      })
      .catch(() => { /* sem rascunho, ok */ });

    nutricaoService.listarCatalogoRacoes().then(setCatalogo).catch(() => {});
    nutricaoService.historicoEvolutivo(pacienteId).then(setHistorico).catch(() => {});
  }, [medicoService, nutricaoService, pacienteId]);

  // Recomendações de ração ao vivo (mudam quando peso ideal / comorbidade / idade muda)
  useEffect(() => {
    if (parametros.pesoIdealKg <= 0 || idadeAnos < 0) {
      setRecomendacoes([]);
      return;
    }
    const timeout = window.setTimeout(() => {
      nutricaoService.recomendarRacoes({
        pesoIdealKg: parametros.pesoIdealKg,
        idadeAnos,
        comorbidade: parametros.comorbidade,
        topN: 3,
      }).then(setRecomendacoes).catch(() => setRecomendacoes([]));
    }, 350);
    return () => window.clearTimeout(timeout);
  }, [parametros.pesoIdealKg, parametros.comorbidade, idadeAnos, nutricaoService]);

  // Preview NEM ao vivo (debounced)
  useEffect(() => {
    const params = parametros;
    if (params.pesoIdealKg <= 0 || params.densidadeCaloricaKcalPorKg <= 0) {
      setPreview(null);
      setErroPreview(null);
      return;
    }
    const timeout = window.setTimeout(() => {
      nutricaoService.preview(params)
        .then(p => { setPreview(p); setErroPreview(null); })
        .catch((e: Error) => { setPreview(null); setErroPreview(e.message); });
    }, 300);
    return () => window.clearTimeout(timeout);
  }, [parametros, nutricaoService]);

  function aplicarTipoCronograma(novo: TipoCronograma) {
    setTipoCronograma(novo);
    if (novo !== "PERSONALIZADO") {
      setDiasCronograma(CRONOGRAMAS_PADRAO[novo]);
    }
  }

  function alterarLinhaCronograma(indice: number, campo: keyof DiaTransicaoDTO, valor: string) {
    setDiasCronograma(prev => prev.map((dia, i) => {
      if (i !== indice) return dia;
      if (campo === "faixaDias") return { ...dia, faixaDias: valor };
      const numero = Number.parseInt(valor || "0", 10);
      return { ...dia, [campo]: Number.isNaN(numero) ? 0 : numero };
    }));
  }

  function adicionarLinhaCronograma() {
    setDiasCronograma(prev => [...prev, { faixaDias: "Novo dia", percentualRacaoAtual: 0, percentualRacaoNova: 100 }]);
  }

  function removerLinhaCronograma(indice: number) {
    setDiasCronograma(prev => prev.filter((_, i) => i !== indice));
  }

  function adicionarObservacao() {
    const texto = novaObservacao.trim();
    if (!texto) return;
    setObservacoes(prev => [...prev, texto]);
    setNovaObservacao("");
  }

  function removerObservacao(indice: number) {
    setObservacoes(prev => prev.filter((_, i) => i !== indice));
  }

  function selecionarRacao(racao: RacaoDTO | null) {
    setRacaoSelecionadaId(racao?.id ?? null);
    if (racao) {
      // Auto-preenche densidade calórica com a da ração escolhida.
      const densidade = typeof racao.densidadeCaloricaKcalPorKg === "number"
        ? racao.densidadeCaloricaKcalPorKg
        : Number.parseFloat(racao.densidadeCaloricaKcalPorKg);
      if (!Number.isNaN(densidade)) {
        setParametros(prev => ({ ...prev, densidadeCaloricaKcalPorKg: densidade }));
      }
    }
  }

  async function salvarRascunho() {
    if (!pacienteId) return;
    if (!tutorId) {
      setErro("Tutor do paciente não foi resolvido. Recarregue a tela.");
      return;
    }
    setErro(null);
    setSalvando(true);
    try {
      const cronograma: CronogramaDTO = { tipo: tipoCronograma, dias: diasCronograma };
      const salvo = await nutricaoService.salvarRascunho({
        pacienteId, tutorId, parametros, cronograma, observacoes,
        racaoId: racaoSelecionadaId,
        justificativaDivergencia: justificativaDivergencia.trim() || null,
      });
      setPlanoRascunho(salvo);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setSalvando(false);
    }
  }

  function abrirAssinatura() {
    if (!planoRascunho) {
      setErro("Salve o rascunho antes de finalizar.");
      return;
    }
    setAssinaturaVazia(true);
    setMostrandoAssinatura(true);
  }

  async function confirmarFinalizacao() {
    if (!planoRascunho) return;
    const imagem = padRef.current?.toDataURL();
    if (!imagem) {
      setErro("Assinatura é obrigatória para finalizar.");
      return;
    }
    setErro(null);
    setFinalizando(true);
    try {
      const finalizado = await nutricaoService.finalizar(planoRascunho.id, imagem);
      setPlanoFinalizado(finalizado);
      setPlanoRascunho(null);
      setMostrandoAssinatura(false);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setFinalizando(false);
    }
  }

  // ── Render ────────────────────────────────────────────────────────────────

  if (planoFinalizado) {
    return (
      <PdfPrescricao
        plano={planoFinalizado}
        nomePet={nomePet}
        nomeTutor={nomeTutor}
        catalogo={catalogo}
        onVoltar={() => navigate(`/medico/prontuario/${pacienteId}`)}
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="flex items-center justify-between">
        <button onClick={() => navigate(`/medico/prontuario/${pacienteId}`)} className="btn-ghost text-sm">
          ← Voltar ao Prontuário
        </button>
        <h1 className="text-xl font-bold text-ink-900">Gestão Nutricional — {nomePet}</h1>
        <div className="w-32" />
      </div>

      {erro && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {erro}
        </div>
      )}

      {/* Parâmetros */}
      <div className="card p-6 space-y-4">
        <h2 className="text-base font-semibold text-ink-900">Parâmetros do Paciente</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <CampoNumero
            rotulo="Peso atual (kg)"
            valor={parametros.pesoAtualKg}
            onChange={v => setParametros({ ...parametros, pesoAtualKg: v })}
            passo={0.1}
          />
          <CampoNumero
            rotulo="Peso ideal (kg)"
            valor={parametros.pesoIdealKg}
            onChange={v => setParametros({ ...parametros, pesoIdealKg: v })}
            passo={0.1}
          />
          <CampoSelect<NivelAtividade>
            rotulo="Nível de Atividade"
            valor={parametros.nivelAtividade}
            onChange={v => setParametros({ ...parametros, nivelAtividade: v })}
            opcoes={Object.entries(ROTULOS_NIVEL_ATIVIDADE) as [NivelAtividade, string][]}
          />
          <CampoSelect<Comorbidade>
            rotulo="Comorbidade"
            valor={parametros.comorbidade}
            onChange={v => setParametros({ ...parametros, comorbidade: v })}
            opcoes={Object.entries(ROTULOS_COMORBIDADE) as [Comorbidade, string][]}
          />
          <CampoNumero
            rotulo="Densidade calórica da ração (kcal/kg)"
            valor={parametros.densidadeCaloricaKcalPorKg}
            onChange={v => setParametros({ ...parametros, densidadeCaloricaKcalPorKg: v })}
            passo={50}
          />
        </div>
      </div>

      {/* Preview NEM */}
      <div className="card p-6 space-y-3">
        <h2 className="text-base font-semibold text-ink-900">Cálculo NEM (Necessidade Energética de Manutenção)</h2>
        {erroPreview && (
          <p className="text-sm text-amber-700">{erroPreview}</p>
        )}
        {!preview && !erroPreview && (
          <p className="text-sm text-ink-500">
            Preencha peso ideal e densidade calórica para ver o cálculo.
          </p>
        )}
        {preview && (
          <>
            {preview.avaliacaoCorporal.exigeAlerta && (
              <div role="alert" className="rounded-xl border-2 border-red-300 bg-red-50 p-3 text-sm text-red-800">
                <strong>⚠ Alerta de Manejo Crítico — {preview.avaliacaoCorporal.classificacao}.</strong>{" "}
                Divergência de {fmt(preview.avaliacaoCorporal.divergenciaPercentual, 1)}% em relação ao peso ideal
                (limite seguro: 15%).
              </div>
            )}
            <dl className="grid grid-cols-2 gap-x-6 gap-y-3 sm:grid-cols-3">
              <Metrica rotulo="Peso metabólico (P^0.75)" valor={fmt(preview.nem.pesoMetabolico, 3)} />
              <Metrica rotulo="NEM base" valor={`${fmt(preview.nem.nemBase, 0)} kcal/dia`} />
              <Metrica rotulo="× Fator atividade" valor={fmt(preview.nem.fatorAtividade, 2)} />
              <Metrica rotulo="× Modif. comorbidade" valor={fmt(preview.nem.modificadorComorbidade, 2)} />
              <Metrica rotulo="NEM total" valor={`${fmt(preview.nem.nemTotal, 0)} kcal/dia`} destaque />
              <Metrica
                rotulo="Quantidade recomendada"
                valor={`${fmt(preview.nem.quantidadeRecomendadaGramasPorDia, 0)} g/dia`}
                destaque
              />
            </dl>
          </>
        )}
      </div>

      {/* Ração Recomendada (Strategy do domínio) */}
      <SecaoRacao
        recomendacoes={recomendacoes}
        catalogo={catalogo}
        racaoSelecionadaId={racaoSelecionadaId}
        onSelecionar={selecionarRacao}
      />

      {/* Justificativa de divergência (condicional, RN reforçada) */}
      {planoRascunho && parseFloat(String(planoRascunho.divergenciaPercentual ?? "0")) > 30 && (
        <div className="card p-6 space-y-3 border-2 border-amber-300">
          <h2 className="text-base font-semibold text-amber-800">
            ⚠ Justificativa Clínica Obrigatória
          </h2>
          <p className="text-sm text-amber-700">
            A divergência entre peso atual e ideal é de {fmt(planoRascunho.divergenciaPercentual, 1)}%
            (acima do limite seguro de 30%). Registre a conduta clínica para finalizar o plano.
          </p>
          <textarea
            value={justificativaDivergencia}
            onChange={e => setJustificativaDivergencia(e.target.value)}
            placeholder="Ex.: paciente em protocolo de perda de peso supervisionado com meta de 60 dias…"
            rows={3}
            className="w-full rounded-lg border border-amber-300 px-3 py-2 text-sm"
          />
        </div>
      )}

      {/* Histórico evolutivo */}
      {historico && historico.historico.length > 0 && (
        <SecaoHistoricoEvolutivo historico={historico} />
      )}

      {/* Cronograma */}
      <div className="card p-6 space-y-4">
        <h2 className="text-base font-semibold text-ink-900">Cronograma de Transição Alimentar</h2>
        <div className="flex flex-wrap gap-2">
          {(Object.keys(ROTULOS_TIPO_CRONOGRAMA) as TipoCronograma[]).map(tipo => (
            <button
              key={tipo}
              type="button"
              onClick={() => aplicarTipoCronograma(tipo)}
              className={
                "rounded-lg border px-3 py-1.5 text-xs font-medium transition " +
                (tipoCronograma === tipo
                  ? "border-brand-400 bg-brand-50 text-brand-700"
                  : "border-ink-300 text-ink-700 hover:bg-ink-50")
              }
            >
              {ROTULOS_TIPO_CRONOGRAMA[tipo]}
            </button>
          ))}
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-ink-200 text-left text-xs text-ink-500">
                <th className="py-2 pr-3">Faixa</th>
                <th className="py-2 pr-3">% Ração Atual</th>
                <th className="py-2 pr-3">% Ração Nova</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {diasCronograma.map((dia, i) => (
                <tr key={i} className="border-b border-ink-100">
                  <td className="py-2 pr-3">
                    <input
                      type="text"
                      value={dia.faixaDias}
                      onChange={e => alterarLinhaCronograma(i, "faixaDias", e.target.value)}
                      disabled={tipoCronograma !== "PERSONALIZADO"}
                      className="w-32 rounded border border-ink-300 px-2 py-1 disabled:bg-ink-50 disabled:text-ink-500"
                    />
                  </td>
                  <td className="py-2 pr-3">
                    <input
                      type="number"
                      min={0}
                      max={100}
                      value={dia.percentualRacaoAtual}
                      onChange={e => alterarLinhaCronograma(i, "percentualRacaoAtual", e.target.value)}
                      disabled={tipoCronograma !== "PERSONALIZADO"}
                      className="w-20 rounded border border-ink-300 px-2 py-1 disabled:bg-ink-50 disabled:text-ink-500"
                    />
                  </td>
                  <td className="py-2 pr-3">
                    <input
                      type="number"
                      min={0}
                      max={100}
                      value={dia.percentualRacaoNova}
                      onChange={e => alterarLinhaCronograma(i, "percentualRacaoNova", e.target.value)}
                      disabled={tipoCronograma !== "PERSONALIZADO"}
                      className="w-20 rounded border border-ink-300 px-2 py-1 disabled:bg-ink-50 disabled:text-ink-500"
                    />
                  </td>
                  <td className="py-2 text-right">
                    {tipoCronograma === "PERSONALIZADO" && (
                      <button
                        type="button"
                        onClick={() => removerLinhaCronograma(i)}
                        className="text-xs text-red-600 hover:underline"
                      >
                        remover
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {tipoCronograma === "PERSONALIZADO" && (
          <button
            type="button"
            onClick={adicionarLinhaCronograma}
            className="rounded-lg border border-dashed border-ink-300 px-3 py-1.5 text-xs text-ink-600 hover:bg-ink-50"
          >
            + Adicionar linha
          </button>
        )}
        <p className="text-xs text-ink-500">
          Cada linha deve somar 100% (ração atual + ração nova).
        </p>
      </div>

      {/* Observações */}
      <div className="card p-6 space-y-3">
        <h2 className="text-base font-semibold text-ink-900">Observações Nutricionais</h2>
        <ul className="space-y-2">
          {observacoes.map((obs, i) => (
            <li key={i} className="flex items-start gap-2 rounded-lg border border-ink-200 bg-ink-50/40 px-3 py-2 text-sm">
              <span className="flex-1">• {obs}</span>
              <button
                type="button"
                onClick={() => removerObservacao(i)}
                className="text-xs text-red-600 hover:underline"
              >
                remover
              </button>
            </li>
          ))}
          {observacoes.length === 0 && (
            <li className="text-sm text-ink-500">Nenhuma observação registrada.</li>
          )}
        </ul>
        <div className="flex gap-2">
          <textarea
            value={novaObservacao}
            onChange={e => setNovaObservacao(e.target.value)}
            placeholder="Ex.: fracionar em 3 refeições por dia."
            className="flex-1 rounded-lg border border-ink-300 px-3 py-2 text-sm"
            rows={2}
          />
          <button
            type="button"
            onClick={adicionarObservacao}
            className="rounded-lg border border-brand-400 bg-brand-50 px-4 py-2 text-sm font-medium text-brand-700 hover:bg-brand-100"
          >
            Adicionar
          </button>
        </div>
      </div>

      {/* Ações */}
      <div className="flex flex-wrap justify-end gap-3">
        <button
          type="button"
          onClick={salvarRascunho}
          disabled={salvando}
          className="rounded-xl border border-ink-300 bg-white px-5 py-2 text-sm font-medium text-ink-700 hover:bg-ink-50 disabled:opacity-60"
        >
          {salvando ? "Salvando…" : "Salvar Rascunho"}
        </button>
        <button
          type="button"
          onClick={abrirAssinatura}
          disabled={!planoRascunho}
          className="rounded-xl border border-brand-500 bg-brand-500 px-5 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
          title={!planoRascunho ? "Salve o rascunho primeiro" : ""}
        >
          Finalizar e Assinar
        </button>
      </div>

      {/* Modal de assinatura */}
      {mostrandoAssinatura && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 px-4">
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
            <h3 className="mb-2 text-base font-semibold text-ink-900">Assinatura Digital</h3>
            <p className="mb-4 text-sm text-ink-600">
              Sua assinatura será anexada ao plano nutricional, que se tornará imutável.
            </p>
            <SignaturePad ref={padRef} onChange={setAssinaturaVazia} />
            <div className="mt-5 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setMostrandoAssinatura(false)}
                disabled={finalizando}
                className="btn-ghost text-sm"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={confirmarFinalizacao}
                disabled={assinaturaVazia || finalizando}
                className="rounded-xl border border-brand-500 bg-brand-500 px-5 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
              >
                {finalizando ? "Finalizando…" : "Confirmar e Finalizar"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Subcomponentes ───────────────────────────────────────────────────────────

function SecaoRacao({
  recomendacoes, catalogo, racaoSelecionadaId, onSelecionar,
}: {
  recomendacoes: RacaoRecomendadaDTO[];
  catalogo: RacaoDTO[];
  racaoSelecionadaId: string | null;
  onSelecionar: (r: RacaoDTO | null) => void;
}) {
  return (
    <div className="card p-6 space-y-4">
      <div className="flex items-baseline justify-between">
        <h2 className="text-base font-semibold text-ink-900">Ração Recomendada</h2>
        <span className="text-xs text-ink-500">Top 3 por afinidade clínica (Strategy)</span>
      </div>

      {recomendacoes.length === 0 && (
        <p className="text-sm text-ink-500">
          Preencha peso ideal e comorbidade para ver sugestões automáticas do catálogo.
        </p>
      )}

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        {recomendacoes.map(rec => {
          const ativa = racaoSelecionadaId === rec.racao.id;
          return (
            <button
              key={rec.racao.id}
              type="button"
              onClick={() => onSelecionar(ativa ? null : rec.racao)}
              className={
                "rounded-xl border-2 p-4 text-left transition " +
                (ativa
                  ? "border-brand-500 bg-brand-50"
                  : "border-ink-200 bg-white hover:border-brand-300 hover:bg-brand-50/30")
              }
            >
              <div className="flex items-start justify-between gap-2">
                <p className="text-sm font-semibold text-ink-900">{rec.racao.descricaoCurta}</p>
                <span className="inline-flex shrink-0 items-center rounded-full bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700 ring-1 ring-brand-200">
                  {rec.pontuacao} pts
                </span>
              </div>
              <p className="mt-1 text-xs text-ink-500">
                {fmt(rec.racao.densidadeCaloricaKcalPorKg, 0)} kcal/kg
              </p>
              {rec.motivosFortes.length > 0 && (
                <p className="mt-2 text-xs text-ink-600">
                  Indicada por: <strong>{rec.motivosFortes.join(", ")}</strong>
                </p>
              )}
            </button>
          );
        })}
      </div>

      <details className="rounded-lg border border-ink-200 p-3">
        <summary className="cursor-pointer text-sm font-medium text-ink-700">
          Ver catálogo completo ({catalogo.length} rações)
        </summary>
        <div className="mt-3 space-y-2 max-h-60 overflow-y-auto">
          {catalogo.map(r => (
            <label key={r.id} className="flex items-center justify-between rounded-lg border border-ink-100 px-3 py-2 text-sm hover:bg-ink-50">
              <span>
                <strong>{r.descricaoCurta}</strong>
                <span className="ml-2 text-xs text-ink-500">
                  {fmt(r.densidadeCaloricaKcalPorKg, 0)} kcal/kg · {r.faixasIndicadas.join("/")} · {r.portesIndicados.join("/")}
                </span>
              </span>
              <input
                type="radio"
                name="racaoCatalogo"
                checked={racaoSelecionadaId === r.id}
                onChange={() => onSelecionar(r)}
              />
            </label>
          ))}
        </div>
        {racaoSelecionadaId && (
          <button
            type="button"
            onClick={() => onSelecionar(null)}
            className="mt-2 text-xs text-red-600 hover:underline"
          >
            Desvincular ração escolhida
          </button>
        )}
      </details>
    </div>
  );
}

function SecaoHistoricoEvolutivo({ historico }: { historico: HistoricoEvolutivoDTO }) {
  const ultima = historico.evolucoes[historico.evolucoes.length - 1];
  const pesos = historico.historico.map(p => Number(p.parametros.pesoAtualKg));
  const maxPeso = Math.max(...pesos);
  const minPeso = Math.min(...pesos);
  const range = Math.max(1, maxPeso - minPeso);

  return (
    <div className="card p-6 space-y-4">
      <div className="flex items-baseline justify-between">
        <h2 className="text-base font-semibold text-ink-900">Evolução Nutricional</h2>
        <span className="text-xs text-ink-500">{historico.historico.length} plano(s) finalizado(s)</span>
      </div>

      {ultima && (
        <div className={
          "rounded-xl border-2 p-4 " +
          (ultima.tendenciaPeso === "GANHO" ? "border-amber-300 bg-amber-50" :
           ultima.tendenciaPeso === "PERDA" ? "border-emerald-300 bg-emerald-50" :
                                              "border-ink-200 bg-ink-50")
        }>
          <p className="text-sm font-medium text-ink-900">
            Desde o último plano:
            {" "}
            <span className={
              "font-bold " +
              (ultima.tendenciaPeso === "GANHO" ? "text-amber-700" :
               ultima.tendenciaPeso === "PERDA" ? "text-emerald-700" : "text-ink-700")
            }>
              {ultima.tendenciaPeso === "ESTAVEL" ? "Estável" :
                `${Number(ultima.deltaPesoKg) > 0 ? "+" : ""}${fmt(ultima.deltaPesoKg, 2)} kg`}
            </span>
          </p>
          <p className="mt-1 text-xs text-ink-600">
            Variação NEM: {Number(ultima.deltaNemPercentual) > 0 ? "+" : ""}{fmt(ultima.deltaNemPercentual, 1)}%
            · Peso anterior: {fmt(ultima.pesoAtualAnteriorKg, 1)} kg → Peso atual: {fmt(ultima.pesoAtualNovoKg, 1)} kg
          </p>
        </div>
      )}

      {/* Mini-gráfico SVG inline (sem libs externas) */}
      {historico.historico.length >= 2 && (
        <div>
          <p className="mb-2 text-xs text-ink-500">Peso ao longo dos planos</p>
          <svg viewBox="0 0 400 100" className="w-full h-24">
            {pesos.map((peso, i) => {
              if (i === 0) return null;
              const x1 = ((i - 1) / (pesos.length - 1)) * 380 + 10;
              const x2 = (i / (pesos.length - 1)) * 380 + 10;
              const y1 = 90 - ((pesos[i - 1] - minPeso) / range) * 70;
              const y2 = 90 - ((peso - minPeso) / range) * 70;
              return (
                <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#02AAB5" strokeWidth="2" />
              );
            })}
            {pesos.map((peso, i) => {
              const x = (i / (pesos.length - 1)) * 380 + 10;
              const y = 90 - ((peso - minPeso) / range) * 70;
              return <circle key={i} cx={x} cy={y} r="3" fill="#02AAB5" />;
            })}
          </svg>
          <div className="flex justify-between text-xs text-ink-400">
            <span>{fmt(minPeso, 1)} kg</span>
            <span>{fmt(maxPeso, 1)} kg</span>
          </div>
        </div>
      )}
    </div>
  );
}

function CampoNumero({
  rotulo, valor, onChange, passo = 1,
}: {
  rotulo: string; valor: number; onChange: (v: number) => void; passo?: number;
}) {
  return (
    <label className="block">
      <span className="text-xs text-ink-500">{rotulo}</span>
      <input
        type="number"
        value={valor}
        step={passo}
        min={0}
        onChange={e => onChange(Number.parseFloat(e.target.value || "0"))}
        className="mt-1 w-full rounded-lg border border-ink-300 px-3 py-2 text-sm"
      />
    </label>
  );
}

function CampoSelect<T extends string>({
  rotulo, valor, onChange, opcoes,
}: {
  rotulo: string; valor: T; onChange: (v: T) => void; opcoes: [T, string][];
}) {
  return (
    <label className="block">
      <span className="text-xs text-ink-500">{rotulo}</span>
      <select
        value={valor}
        onChange={e => onChange(e.target.value as T)}
        className="mt-1 w-full rounded-lg border border-ink-300 px-3 py-2 text-sm"
      >
        {opcoes.map(([v, r]) => (
          <option key={v} value={v}>{r}</option>
        ))}
      </select>
    </label>
  );
}

function Metrica({ rotulo, valor, destaque = false }: { rotulo: string; valor: string; destaque?: boolean }) {
  return (
    <div>
      <dt className="text-xs text-ink-500">{rotulo}</dt>
      <dd className={"mt-0.5 text-sm font-medium " + (destaque ? "text-brand-700 text-base" : "text-ink-900")}>
        {valor}
      </dd>
    </div>
  );
}

// ── PDF / Tela de impressão ──────────────────────────────────────────────────

function PdfPrescricao({
  plano, nomePet, nomeTutor, onVoltar, catalogo,
}: {
  plano: PlanoNutricionalDTO; nomePet: string; nomeTutor: string; onVoltar: () => void;
  catalogo: RacaoDTO[];
}) {
  const resultado = plano.resultadoFinalizado!;
  const assinatura = plano.assinatura!;
  const dataAssinatura = new Date(assinatura.assinadoEm).toLocaleString("pt-BR");
  const racaoPrescrita = plano.racaoId
    ? catalogo.find(r => r.id === plano.racaoId) ?? null
    : null;

  return (
    <div className="space-y-4">
      {/* Barra de ações — escondida na impressão */}
      <div className="flex flex-wrap items-center justify-between gap-3 print:hidden">
        <button onClick={onVoltar} className="btn-ghost text-sm">← Voltar ao Prontuário</button>
        <button
          type="button"
          onClick={() => window.print()}
          className="rounded-xl border border-brand-500 bg-brand-500 px-5 py-2 text-sm font-medium text-white hover:bg-brand-600"
        >
          🖨 Gerar PDF (Imprimir)
        </button>
      </div>

      {/* Conteúdo imprimível */}
      <div className="card p-8 space-y-6 print:shadow-none print:border-0">
        <header className="border-b border-ink-200 pb-4">
          <h1 className="text-2xl font-bold text-ink-900">Prescrição Nutricional</h1>
          <p className="mt-1 text-sm text-ink-500">
            Plano nº <code>{plano.id}</code>
          </p>
          <dl className="mt-4 grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <div><dt className="text-xs text-ink-500">Paciente</dt><dd className="font-medium">{nomePet}</dd></div>
            <div><dt className="text-xs text-ink-500">Tutor</dt><dd className="font-medium">{nomeTutor}</dd></div>
            <div><dt className="text-xs text-ink-500">Emitido em</dt><dd className="font-medium">{dataAssinatura}</dd></div>
            <div><dt className="text-xs text-ink-500">Status</dt><dd className="font-medium text-brand-700">{plano.status}</dd></div>
          </dl>
        </header>

        <section>
          <h2 className="mb-2 text-base font-semibold text-ink-900">Parâmetros Considerados</h2>
          <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm sm:grid-cols-3">
            <Linha rotulo="Peso atual" valor={`${plano.parametros.pesoAtualKg} kg`} />
            <Linha rotulo="Peso ideal" valor={`${plano.parametros.pesoIdealKg} kg`} />
            <Linha rotulo="Nível de atividade" valor={ROTULOS_NIVEL_ATIVIDADE[plano.parametros.nivelAtividade]} />
            <Linha rotulo="Comorbidade" valor={ROTULOS_COMORBIDADE[plano.parametros.comorbidade]} />
            <Linha rotulo="Densidade calórica" valor={`${plano.parametros.densidadeCaloricaKcalPorKg} kcal/kg`} />
          </dl>
        </section>

        <section>
          <h2 className="mb-2 text-base font-semibold text-ink-900">Cálculo NEM</h2>
          <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm sm:grid-cols-3">
            <Linha rotulo="Peso metabólico" valor={fmt(resultado.pesoMetabolico, 3)} />
            <Linha rotulo="NEM base" valor={`${fmt(resultado.nemBase, 0)} kcal/dia`} />
            <Linha rotulo="× Fator atividade" valor={fmt(resultado.fatorAtividade, 2)} />
            <Linha rotulo="× Modificador comorbidade" valor={fmt(resultado.modificadorComorbidade, 2)} />
            <Linha rotulo="NEM total" valor={`${fmt(resultado.nemTotal, 0)} kcal/dia`} destaque />
            <Linha
              rotulo="Quantidade recomendada"
              valor={`${fmt(resultado.quantidadeRecomendadaGramasPorDia, 0)} g/dia`}
              destaque
            />
          </dl>
        </section>

        {racaoPrescrita && (
          <section>
            <h2 className="mb-2 text-base font-semibold text-ink-900">Ração Prescrita</h2>
            <p className="text-sm font-medium text-ink-900">{racaoPrescrita.descricaoCurta}</p>
            <p className="text-xs text-ink-500">
              {fmt(racaoPrescrita.densidadeCaloricaKcalPorKg, 0)} kcal/kg
              · Indicada para: {racaoPrescrita.faixasIndicadas.join(", ")}
              · Portes: {racaoPrescrita.portesIndicados.join(", ")}
            </p>
          </section>
        )}

        {plano.justificativaDivergencia && (
          <section className="rounded-lg border border-amber-200 bg-amber-50 p-3">
            <h2 className="mb-1 text-sm font-semibold text-amber-800">
              Justificativa Clínica (divergência {fmt(plano.divergenciaPercentual, 1)}%)
            </h2>
            <p className="text-sm text-amber-900">{plano.justificativaDivergencia}</p>
          </section>
        )}

        <section>
          <h2 className="mb-2 text-base font-semibold text-ink-900">
            Cronograma de Transição — {ROTULOS_TIPO_CRONOGRAMA[plano.cronograma.tipo]}
          </h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-ink-200 text-left text-xs text-ink-500">
                <th className="py-2 pr-3">Faixa</th>
                <th className="py-2 pr-3">% Ração Atual</th>
                <th className="py-2">% Ração Nova</th>
              </tr>
            </thead>
            <tbody>
              {plano.cronograma.dias.map((dia, i) => (
                <tr key={i} className="border-b border-ink-100">
                  <td className="py-1.5 pr-3">{dia.faixaDias}</td>
                  <td className="py-1.5 pr-3">{dia.percentualRacaoAtual}%</td>
                  <td className="py-1.5">{dia.percentualRacaoNova}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>

        {plano.observacoes.length > 0 && (
          <section>
            <h2 className="mb-2 text-base font-semibold text-ink-900">Observações</h2>
            <ul className="list-disc space-y-1 pl-5 text-sm">
              {plano.observacoes.map((o, i) => <li key={i}>{o}</li>)}
            </ul>
          </section>
        )}

        <section className="border-t border-ink-200 pt-4">
          <h2 className="mb-2 text-base font-semibold text-ink-900">Assinatura Digital</h2>
          <img
            src={assinatura.imagemBase64}
            alt="Assinatura do médico"
            className="mb-2 h-28 w-auto rounded border border-ink-200 bg-white p-2"
          />
          <p className="text-xs text-ink-500">
            Assinado em {dataAssinatura} · Hash SHA-256: <code>{assinatura.hashConteudo}</code>
          </p>
        </section>
      </div>
    </div>
  );
}

function Linha({ rotulo, valor, destaque = false }: { rotulo: string; valor: string; destaque?: boolean }) {
  return (
    <div>
      <dt className="text-xs text-ink-500">{rotulo}</dt>
      <dd className={"mt-0.5 " + (destaque ? "text-brand-700 font-semibold" : "text-ink-900 font-medium")}>
        {valor}
      </dd>
    </div>
  );
}
