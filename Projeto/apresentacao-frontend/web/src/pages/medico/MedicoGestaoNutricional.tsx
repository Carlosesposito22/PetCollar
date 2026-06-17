import { Fragment, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
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

  // Plano finalizado (só após assinatura — não há rascunho persistido)
  const [planoFinalizado, setPlanoFinalizado] = useState<PlanoNutricionalDTO | null>(null);
  // Plano vigente do paciente — se existir, será SUBSTITUIDO ao finalizar o novo
  const [planoVigente, setPlanoVigente] = useState<PlanoNutricionalDTO | null>(null);

  // Preview ao vivo
  const [preview, setPreview] = useState<PreviewNEMDTO | null>(null);
  const [erroPreview, setErroPreview] = useState<string | null>(null);

  // Modal de assinatura
  const [mostrandoAssinatura, setMostrandoAssinatura] = useState(false);
  const [assinaturaVazia, setAssinaturaVazia] = useState(true);
  const padRef = useRef<SignaturePadHandle>(null);

  // Feedback geral
  const [erro, setErro] = useState<string | null>(null);
  const [finalizando, setFinalizando] = useState(false);

  // Divergência peso atual ↔ ideal (mesma fórmula do backend: |atual - ideal| / ideal * 100)
  const divergenciaPercentual = parametros.pesoIdealKg > 0
    ? Math.abs(parametros.pesoAtualKg - parametros.pesoIdealKg) / parametros.pesoIdealKg * 100
    : 0;

  // Carregamento inicial: contexto do paciente (tutorId real do banco) +
  // rascunho existente + catálogo de rações + histórico de planos.
  useEffect(() => {
    if (!pacienteId) return;
    nutricaoService.contextoPaciente(pacienteId)
      .then(ctx => {
        setNomePet(ctx.nomePet);
        setNomeTutor(ctx.nomeTutor);
        setTutorId(ctx.tutorId);
        setIdadeAnos(ctx.idadeAnos);
        const pesoAtual = typeof ctx.pesoAtualKg === "number"
          ? ctx.pesoAtualKg
          : Number.parseFloat(String(ctx.pesoAtualKg));
        if (!Number.isNaN(pesoAtual) && pesoAtual > 0) {
          setParametros(prev =>
            prev.pesoAtualKg === 0 ? { ...prev, pesoAtualKg: pesoAtual } : prev);
        }
      })
      .catch((e: Error) => setErro(`Não foi possível resolver o paciente: ${e.message}`));

    nutricaoService.listarCatalogoRacoes().then(setCatalogo).catch(() => {});
    nutricaoService.historicoEvolutivo(pacienteId).then(setHistorico).catch(() => {});

    // Carrega o plano vigente: pré-preenche o form e avisa o médico que o
    // próximo Finalizar substituirá esta prescrição.
    nutricaoService.buscarVigente(pacienteId)
      .then(vig => {
        if (!vig) return;
        setPlanoVigente(vig);
        setParametros(vig.parametros);
        setTipoCronograma(vig.cronograma.tipo);
        setDiasCronograma(vig.cronograma.dias);
        setObservacoes(vig.observacoes);
        setRacaoSelecionadaId(vig.racaoId);
        setJustificativaDivergencia(vig.justificativaDivergencia ?? "");
      })
      .catch(() => { /* sem vigente, ok */ });
  }, [nutricaoService, pacienteId]);

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

  /** Abre o modal de assinatura (não persiste nada ainda). */
  function abrirAssinatura() {
    if (!tutorId) {
      setErro("Paciente ainda carregando — aguarde alguns instantes e tente de novo.");
      return;
    }
    if (parametros.pesoIdealKg <= 0) {
      setErro("Informe o peso ideal antes de finalizar.");
      return;
    }
    setErro(null);
    setAssinaturaVazia(true);
    setMostrandoAssinatura(true);
  }

  /**
   * Cria + finaliza o plano nutricional em um único POST atômico. Se a
   * validação falhar, nada é persistido no banco — não fica rascunho
   * órfão por aí. A tela só avança para a vista de PDF se der certo.
   */
  async function confirmarFinalizacao() {
    if (!tutorId || !pacienteId) return;
    const imagem = padRef.current?.toDataURL();
    if (!imagem) {
      setErro("Assinatura é obrigatória para finalizar.");
      return;
    }
    setErro(null);
    setFinalizando(true);
    try {
      const cronograma: CronogramaDTO = { tipo: tipoCronograma, dias: diasCronograma };
      const finalizado = await nutricaoService.finalizarDireto({
        pacienteId, tutorId, parametros, cronograma, observacoes,
        racaoId: racaoSelecionadaId,
        justificativaDivergencia: justificativaDivergencia.trim() || null,
        imagemAssinaturaBase64: imagem,
      });
      setPlanoFinalizado(finalizado);
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

      {/* Aviso de substituição (plano vigente carregado) */}
      {planoVigente && (
        <div role="alert" className="rounded-xl border-2 border-amber-300 bg-amber-50 p-4">
          <div className="flex items-start gap-3">
            <span className="text-xl" aria-hidden="true">⚠</span>
            <div className="flex-1 text-sm text-amber-800">
              <p className="font-semibold">
                Este paciente já tem uma prescrição nutricional vigente
                {planoVigente.assinatura && (
                  <>
                    {" "}emitida em{" "}
                    <strong>
                      {new Date(planoVigente.assinatura.assinadoEm).toLocaleDateString("pt-BR")}
                    </strong>
                  </>
                )}
                .
              </p>
              <p className="mt-1">
                Os campos abaixo foram pré-preenchidos com os dados do plano atual.
                Ao clicar em <strong>Finalizar e Assinar</strong>, a prescrição anterior
                será marcada como <strong>SUBSTITUÍDA</strong> (permanece no histórico para auditoria)
                e a nova passa a ser a vigente para o tutor.
              </p>
            </div>
          </div>
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
            placeholder={placeholderPesoIdeal(parametros.pesoAtualKg, idadeAnos)}
            ajuda={ajudaPesoIdeal(parametros.pesoAtualKg, idadeAnos)}
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
      {divergenciaPercentual > 30 && (
        <div className="card p-6 space-y-3 border-2 border-amber-300">
          <h2 className="text-base font-semibold text-amber-800">
            ⚠ Justificativa Clínica Obrigatória
          </h2>
          <p className="text-sm text-amber-700">
            A divergência entre peso atual e ideal é de {divergenciaPercentual.toFixed(1)}%
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
        <SecaoHistoricoEvolutivo historico={historico} catalogo={catalogo} />
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
      <div className="space-y-2">
        <div className="flex justify-end">
          <button
            type="button"
            onClick={abrirAssinatura}
            disabled={!tutorId}
            className="rounded-xl border border-brand-500 bg-brand-500 px-6 py-3 text-base font-semibold text-white hover:bg-brand-600 disabled:opacity-50"
          >
            ✍ {planoVigente ? "Substituir e Assinar" : "Finalizar e Assinar"}
          </button>
        </div>
        <p className="text-right text-xs text-ink-500">
          {planoVigente
            ? "A prescrição vigente será marcada como SUBSTITUÍDA e a nova passa a valer."
            : "O plano é salvo no banco apenas após a assinatura digital — antes disso, nada fica persistido."}
        </p>
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

/** Converte o nome da Strategy do backend em texto humano para o card. */
function motivoHumano(nomeStrategy: string): string {
  switch (nomeStrategy) {
    case "Comorbidade": return "Cobre a comorbidade";
    case "FaixaEtaria": return "Adequada à idade";
    case "Porte":       return "Adequada ao porte";
    default:            return nomeStrategy;
  }
}

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
      <div>
        <h2 className="text-base font-semibold text-ink-900">Ração Recomendada</h2>
        <p className="text-xs text-ink-500">
          O sistema sugere as 3 rações mais adequadas com base no porte, idade e comorbidade do paciente.
          Clique numa para vinculá-la ao plano (a densidade calórica é preenchida automaticamente).
        </p>
      </div>

      {recomendacoes.length === 0 && (
        <p className="text-sm text-ink-500">
          Preencha peso ideal e comorbidade para ver sugestões automáticas do catálogo.
        </p>
      )}

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        {recomendacoes.map((rec, indice) => {
          const ativa = racaoSelecionadaId === rec.racao.id;
          const rotuloMotivos = rec.motivosFortes.map(motivoHumano).join(", ");
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
                <span className="inline-flex shrink-0 items-center rounded-full bg-ink-100 px-2 py-0.5 text-xs font-medium text-ink-600">
                  #{indice + 1}
                </span>
              </div>
              <p className="mt-1 text-xs text-ink-500">
                {fmt(rec.racao.densidadeCaloricaKcalPorKg, 0)} kcal/kg
              </p>
              {rotuloMotivos && (
                <p className="mt-2 text-xs text-ink-600">
                  ✓ {rotuloMotivos}
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

function SecaoHistoricoEvolutivo({
  historico, catalogo,
}: { historico: HistoricoEvolutivoDTO; catalogo: RacaoDTO[] }) {
  if (historico.historico.length === 0) return null;

  // Ordena do mais recente para o mais antigo para a tabela.
  const planosDoMaisRecente = [...historico.historico].sort(
    (a, b) => new Date(b.atualizadoEm).getTime() - new Date(a.atualizadoEm).getTime());

  // O backend devolve as evoluções em ordem cronológica (antigo → novo).
  // Pra cruzar com a tabela (recente → antigo), indexamos por data do plano novo.
  const evolucaoPorDataNovo = new Map<string, typeof historico.evolucoes[number]>();
  historico.evolucoes.forEach(e => evolucaoPorDataNovo.set(e.planoAtualEm, e));

  // Dados pro gráfico (em ordem cronológica, mais antigo → mais novo).
  const cronologico = [...historico.historico].sort(
    (a, b) => new Date(a.atualizadoEm).getTime() - new Date(b.atualizadoEm).getTime());
  const pesos = cronologico.map(p => Number(p.parametros.pesoAtualKg));
  const maxPeso = Math.max(...pesos);
  const minPeso = Math.min(...pesos);
  const range = Math.max(0.1, maxPeso - minPeso);

  return (
    <div className="card p-6 space-y-5">
      <div>
        <h2 className="text-base font-semibold text-ink-900">Evolução Nutricional</h2>
        <p className="mt-0.5 text-xs text-ink-500">
          Histórico cronológico das prescrições nutricionais deste paciente — base para decidir
          ajustes na próxima consulta.
        </p>
      </div>

      {/* Mini-gráfico de peso (só faz sentido com 2+ pontos) */}
      {pesos.length >= 2 && (
        <div className="rounded-lg border border-ink-200 bg-ink-50/40 p-4">
          <p className="mb-2 text-xs font-medium text-ink-600">Peso ao longo das prescrições</p>
          <svg viewBox="0 0 400 100" className="h-24 w-full">
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
              return <circle key={i} cx={x} cy={y} r="3.5" fill="#02AAB5" />;
            })}
          </svg>
          <div className="flex justify-between text-xs text-ink-500">
            <span>{fmt(minPeso, 1)} kg · {new Date(cronologico[0].atualizadoEm).toLocaleDateString("pt-BR")}</span>
            <span>{fmt(maxPeso, 1)} kg · {new Date(cronologico[cronologico.length - 1].atualizadoEm).toLocaleDateString("pt-BR")}</span>
          </div>
        </div>
      )}

      {/* Tabela cronológica com TUDO que foi prescrito */}
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b-2 border-ink-200 text-left text-xs text-ink-500">
              <th className="py-2 pr-3">Data</th>
              <th className="py-2 pr-3">Status</th>
              <th className="py-2 pr-3">Peso atual</th>
              <th className="py-2 pr-3">Peso ideal</th>
              <th className="py-2 pr-3">NEM</th>
              <th className="py-2 pr-3">Ração/dia</th>
              <th className="py-2">Ração escolhida</th>
            </tr>
          </thead>
          <tbody>
            {planosDoMaisRecente.map((p, idx) => {
              const racao = p.racaoId ? catalogo.find(r => r.id === p.racaoId) : null;
              const evolucaoEntrada = evolucaoPorDataNovo.get(p.atualizadoEm);
              const ehMaisRecente = idx === 0;
              return (
                <Fragment key={p.id}>
                  <tr className={"border-b border-ink-100 " + (ehMaisRecente ? "bg-brand-50/30" : "")}>
                    <td className="py-2 pr-3">
                      <div className="font-medium text-ink-900">
                        {new Date(p.atualizadoEm).toLocaleDateString("pt-BR")}
                      </div>
                      <div className="text-xs text-ink-500">
                        {new Date(p.atualizadoEm).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}
                      </div>
                    </td>
                    <td className="py-2 pr-3">
                      <span className={
                        "rounded-full px-2 py-0.5 text-xs font-medium " +
                        (p.status === "FINALIZADO"
                          ? "bg-emerald-100 text-emerald-700"
                          : "bg-ink-100 text-ink-600")
                      }>
                        {p.status === "FINALIZADO" ? "Vigente" : "Substituído"}
                      </span>
                    </td>
                    <td className="py-2 pr-3 font-medium">{fmt(p.parametros.pesoAtualKg, 1)} kg</td>
                    <td className="py-2 pr-3 text-ink-600">{fmt(p.parametros.pesoIdealKg, 1)} kg</td>
                    <td className="py-2 pr-3">
                      {p.resultadoFinalizado
                        ? `${fmt(p.resultadoFinalizado.nemTotal, 0)} kcal`
                        : "—"}
                    </td>
                    <td className="py-2 pr-3">
                      {p.resultadoFinalizado
                        ? `${fmt(p.resultadoFinalizado.quantidadeRecomendadaGramasPorDia, 0)} g`
                        : "—"}
                    </td>
                    <td className="py-2 text-xs text-ink-700">
                      {racao?.descricaoCurta ?? "—"}
                    </td>
                  </tr>
                  {/* Linha de delta — aparece ENTRE planos consecutivos */}
                  {evolucaoEntrada && (
                    <tr className="border-b border-ink-100 bg-ink-50/40">
                      <td colSpan={7} className="py-2 px-3">
                        <LinhaDelta evolucao={evolucaoEntrada} />
                      </td>
                    </tr>
                  )}
                </Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function LinhaDelta({ evolucao }: { evolucao: HistoricoEvolutivoDTO["evolucoes"][number] }) {
  const cor = evolucao.tendenciaPeso === "GANHO" ? "text-amber-700" :
              evolucao.tendenciaPeso === "PERDA" ? "text-emerald-700" : "text-ink-600";
  const seta = evolucao.tendenciaPeso === "GANHO" ? "↑" :
               evolucao.tendenciaPeso === "PERDA" ? "↓" : "→";
  const deltaPeso = Number(evolucao.deltaPesoKg);
  const deltaNem = Number(evolucao.deltaNemPercentual);
  return (
    <div className="flex items-center gap-3 text-xs">
      <span className="text-ink-400">↳ Variação para a prescrição anterior:</span>
      <span className={"font-semibold " + cor}>
        {seta} {evolucao.tendenciaPeso === "ESTAVEL" ? "Peso estável" :
          `${deltaPeso > 0 ? "+" : ""}${fmt(evolucao.deltaPesoKg, 2)} kg (${deltaPeso > 0 ? "+" : ""}${fmt(evolucao.deltaPesoPercentual, 1)}%)`}
      </span>
      <span className="text-ink-500">·</span>
      <span className="text-ink-600">
        NEM {deltaNem > 0 ? "+" : ""}{fmt(evolucao.deltaNemPercentual, 1)}%
      </span>
    </div>
  );
}

function CampoNumero({
  rotulo, valor, onChange, passo = 1, placeholder, ajuda,
}: {
  rotulo: string; valor: number; onChange: (v: number) => void;
  passo?: number; placeholder?: string; ajuda?: string;
}) {
  // Mantém o input como string durante a edição para permitir apagar tudo,
  // digitar "5.5" sem o React forçar de volta pra "5", etc. Só converte
  // pra number na saída via onChange.
  const [texto, setTexto] = useState<string>(valor === 0 ? "" : String(valor));

  useEffect(() => {
    // Sincroniza quando o valor é alterado externamente (auto-preencher
    // pela seleção de ração, por exemplo).
    const numAtual = Number.parseFloat(texto.replace(",", "."));
    if (valor !== numAtual) {
      setTexto(valor === 0 ? "" : String(valor));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [valor]);

  return (
    <label className="block">
      <span className="text-xs text-ink-500">{rotulo}</span>
      <input
        type="text"
        inputMode="decimal"
        value={texto}
        placeholder={placeholder ?? "0,0"}
        onChange={e => {
          const t = e.target.value.replace(/[^0-9.,]/g, "");
          setTexto(t);
          const n = Number.parseFloat(t.replace(",", "."));
          onChange(Number.isNaN(n) ? 0 : n);
        }}
        className="mt-1 w-full rounded-lg border border-ink-300 px-3 py-2 text-sm"
      />
      {ajuda && <span className="mt-1 block text-xs text-ink-500">{ajuda}</span>}
      <span className="sr-only">passo {passo}</span>
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
      {/* Banner de sucesso + ações — escondidos na impressão */}
      <div className="print:hidden rounded-2xl border-2 border-emerald-300 bg-emerald-50 p-5">
        <div className="flex items-start gap-3">
          <span className="text-3xl" aria-hidden="true">✓</span>
          <div className="flex-1">
            <h2 className="text-lg font-bold text-emerald-800">Plano Nutricional Finalizado</h2>
            <p className="mt-1 text-sm text-emerald-700">
              Assinado digitalmente em {dataAssinatura}. O plano agora é imutável e está visível
              para o tutor na aba <strong>Nutrição</strong> dele.
            </p>
          </div>
        </div>
        <div className="mt-4 flex flex-wrap items-center justify-end gap-3">
          <button onClick={onVoltar} className="btn-ghost text-sm">← Voltar ao Prontuário</button>
          <button
            type="button"
            onClick={() => window.print()}
            className="rounded-xl border border-brand-500 bg-brand-500 px-6 py-3 text-base font-semibold text-white shadow-sm hover:bg-brand-600"
          >
            📄 Baixar PDF
          </button>
        </div>
      </div>

      {/* Conteúdo imprimível */}
      <div className="card p-8 space-y-6 print:shadow-none print:border-0">
        <header className="border-b border-ink-200 pb-4">
          <h1 className="text-2xl font-bold text-ink-900">Prescrição Nutricional</h1>
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
            ✓ Documento assinado digitalmente em {dataAssinatura}.
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

// ── Heurística clínica de peso ideal sugerido ────────────────────────────────
//
// Sem um BCS (Body Condition Score) registrado no prontuário, estimamos o
// peso ideal a partir da idade do paciente:
//
//   • Filhote (<1 ano): peso ideal = peso atual (animal em crescimento, sem
//     meta diferente do que ele já está).
//   • Adulto (1–7 anos): peso ideal = peso atual × 1,00 (assume BCS 5/9
//     ideal — sem motivo clínico pra alterar).
//   • Sênior (>7 anos): peso ideal = peso atual × 0,95 (sêniors tendem a
//     ganhar peso pela redução de atividade; sugerimos uma meta conservadora
//     de 5% de redução como ponto de partida da discussão clínica).
//
// É sempre apenas SUGESTÃO no placeholder — o médico digita o valor real
// baseado no exame físico e no peso ideal de referência da raça.

function calcularPesoIdealSugerido(pesoAtualKg: number, idadeAnos: number): number {
  if (pesoAtualKg <= 0) return 0;
  if (idadeAnos < 1) return pesoAtualKg;                   // Filhote
  if (idadeAnos <= 7) return pesoAtualKg;                   // Adulto saudável
  return Math.round(pesoAtualKg * 0.95 * 10) / 10;          // Sênior — 5% conservador
}

function placeholderPesoIdeal(pesoAtualKg: number, idadeAnos: number): string {
  const sugerido = calcularPesoIdealSugerido(pesoAtualKg, idadeAnos);
  return sugerido > 0 ? `Sugestão: ${sugerido}` : "0,0";
}

function ajudaPesoIdeal(pesoAtualKg: number, idadeAnos: number): string | undefined {
  if (pesoAtualKg <= 0) return undefined;
  if (idadeAnos < 1) return "Filhote — sugestão = peso atual (paciente em crescimento).";
  if (idadeAnos <= 7) return "Adulto saudável — sugestão assume BCS 5/9 (peso atual).";
  return "Sênior — sugestão conservadora de −5% (sêniors tendem a ganhar peso por redução de atividade).";
}
