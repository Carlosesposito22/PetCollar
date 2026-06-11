type ApiFetch = (input: string, init?: RequestInit) => Promise<Response>;

export class NutricaoApiError extends Error {
  readonly status: number;
  constructor(status: number, mensagem: string) {
    super(mensagem);
    this.status = status;
    this.name = "NutricaoApiError";
  }
}

async function lancarSeErro(res: Response): Promise<Response> {
  if (res.ok) return res;
  const corpo = await res.json().catch(() => ({}) as { mensagem?: string });
  throw new NutricaoApiError(res.status, corpo?.mensagem ?? `Falha na requisição (HTTP ${res.status}).`);
}

async function json<T>(entrada: Response | Promise<Response>): Promise<T> {
  return (await lancarSeErro(await entrada)).json() as Promise<T>;
}

// ── Tipos de domínio espelhados ─────────────────────────────────────────────

export type NivelAtividade =
  | "SEDENTARIO" | "POUCO_ATIVO" | "MODERADAMENTE_ATIVO" | "MUITO_ATIVO" | "ATLETA";

export type Comorbidade =
  | "NENHUMA" | "OBESIDADE" | "DIABETES" | "DOENCA_RENAL";

export type TipoCronograma =
  | "PADRAO_7_DIAS" | "PADRAO_10_DIAS" | "PADRAO_14_DIAS" | "PERSONALIZADO";

export type StatusPlano = "RASCUNHO" | "FINALIZADO";

export type ParametrosDTO = {
  pesoAtualKg: number;
  pesoIdealKg: number;
  nivelAtividade: NivelAtividade;
  comorbidade: Comorbidade;
  densidadeCaloricaKcalPorKg: number;
};

/** Valores numéricos que vêm de BigDecimal — podem chegar como number ou string. */
export type Decimal = number | string;

export type ResultadoNEMDTO = {
  pesoMetabolico: Decimal;
  nemBase: Decimal;
  fatorAtividade: Decimal;
  modificadorComorbidade: Decimal;
  nemTotal: Decimal;
  quantidadeRecomendadaGramasPorDia: Decimal;
};

export type AvaliacaoCorporalDTO = {
  classificacao: "ADEQUADO" | "OBESIDADE" | "CAQUEXIA";
  divergenciaPercentual: Decimal;
  exigeAlerta: boolean;
};

export type PreviewNEMDTO = {
  nem: ResultadoNEMDTO;
  avaliacaoCorporal: AvaliacaoCorporalDTO;
};

export type DiaTransicaoDTO = {
  faixaDias: string;
  percentualRacaoAtual: number;
  percentualRacaoNova: number;
};

export type CronogramaDTO = {
  tipo: TipoCronograma;
  dias: DiaTransicaoDTO[];
};

export type AssinaturaDigitalDTO = {
  medicoResponsavelId: string;
  imagemBase64: string;
  assinadoEm: string;
  hashConteudo: string;
};

export type PlanoNutricionalDTO = {
  id: string;
  pacienteId: string;
  tutorId: string;
  medicoResponsavelId: string;
  parametros: ParametrosDTO;
  cronograma: CronogramaDTO;
  observacoes: string[];
  status: StatusPlano;
  criadoEm: string;
  atualizadoEm: string;
  resultadoFinalizado: ResultadoNEMDTO | null;
  assinatura: AssinaturaDigitalDTO | null;
  racaoId: string | null;
  justificativaDivergencia: string | null;
  divergenciaPercentual: Decimal;
};

export type RacaoDTO = {
  id: string;
  fabricante: string;
  linha: string;
  descricaoCurta: string;
  densidadeCaloricaKcalPorKg: Decimal;
  faixasIndicadas: string[];
  portesIndicados: string[];
  comorbidadesIndicadas: string[];
};

export type RacaoRecomendadaDTO = {
  racao: RacaoDTO;
  pontuacao: number;
  detalhes: Record<string, number>;
  motivosFortes: string[];
};

export type EvolucaoNutricionalDTO = {
  planoAnteriorEm: string;
  planoAtualEm: string;
  pesoAtualAnteriorKg: Decimal;
  pesoAtualNovoKg: Decimal;
  deltaPesoKg: Decimal;
  deltaPesoPercentual: Decimal;
  nemAnteriorKcal: Decimal;
  nemNovoKcal: Decimal;
  deltaNemPercentual: Decimal;
  tendenciaPeso: "GANHO" | "PERDA" | "ESTAVEL";
};

export type HistoricoEvolutivoDTO = {
  historico: PlanoNutricionalDTO[];
  evolucoes: EvolucaoNutricionalDTO[];
};

export type ContextoPacienteDTO = {
  pacienteId: string;
  tutorId: string;
  nomePet: string;
  nomeTutor: string;
  pesoAtualKg: Decimal;
  idadeAnos: number;
};

// ── Rótulos para UI ─────────────────────────────────────────────────────────

export const ROTULOS_NIVEL_ATIVIDADE: Record<NivelAtividade, string> = {
  SEDENTARIO: "Sedentário (1.2)",
  POUCO_ATIVO: "Pouco Ativo (1.4)",
  MODERADAMENTE_ATIVO: "Moderadamente Ativo (1.6)",
  MUITO_ATIVO: "Muito Ativo (1.8)",
  ATLETA: "Atleta (2.0)",
};

export const ROTULOS_COMORBIDADE: Record<Comorbidade, string> = {
  NENHUMA: "Nenhuma",
  OBESIDADE: "Obesidade (×0.8)",
  DIABETES: "Diabetes (×0.85)",
  DOENCA_RENAL: "Doença Renal (×0.85)",
};

export const ROTULOS_TIPO_CRONOGRAMA: Record<TipoCronograma, string> = {
  PADRAO_7_DIAS: "Padrão 7 dias",
  PADRAO_10_DIAS: "Padrão 10 dias",
  PADRAO_14_DIAS: "Padrão 14 dias",
  PERSONALIZADO: "Personalizado",
};

// ── Service do médico ───────────────────────────────────────────────────────

export function criarNutricaoMedicoService(apiFetch: ApiFetch) {
  return {
    preview: (parametros: ParametrosDTO): Promise<PreviewNEMDTO> =>
      json<PreviewNEMDTO>(
        apiFetch("/api/medico/nutricao/preview", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ parametros }),
        })
      ),

    salvarRascunho: (input: {
      pacienteId: string;
      tutorId: string;
      parametros: ParametrosDTO;
      cronograma: CronogramaDTO;
      observacoes: string[];
      racaoId?: string | null;
      justificativaDivergencia?: string | null;
    }): Promise<PlanoNutricionalDTO> =>
      json<PlanoNutricionalDTO>(
        apiFetch("/api/medico/nutricao/rascunho", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(input),
        })
      ),

    listarCatalogoRacoes: (): Promise<RacaoDTO[]> =>
      json<RacaoDTO[]>(apiFetch("/api/medico/nutricao/racoes")),

    recomendarRacoes: (params: {
      pesoIdealKg: number;
      idadeAnos: number;
      comorbidade: Comorbidade;
      topN?: number;
    }): Promise<RacaoRecomendadaDTO[]> => {
      const q = new URLSearchParams({
        pesoIdealKg: String(params.pesoIdealKg),
        idadeAnos: String(params.idadeAnos),
        comorbidade: params.comorbidade,
        topN: String(params.topN ?? 3),
      });
      return json<RacaoRecomendadaDTO[]>(
        apiFetch(`/api/medico/nutricao/racoes/recomendacoes?${q.toString()}`)
      );
    },

    historicoEvolutivo: (pacienteId: string): Promise<HistoricoEvolutivoDTO> =>
      json<HistoricoEvolutivoDTO>(
        apiFetch(`/api/medico/nutricao/pacientes/${pacienteId}/evolucao`)
      ),

    contextoPaciente: (pacienteId: string): Promise<ContextoPacienteDTO> =>
      json<ContextoPacienteDTO>(
        apiFetch(`/api/medico/nutricao/pacientes/${pacienteId}/contexto`)
      ),

    buscarRascunho: async (pacienteId: string): Promise<PlanoNutricionalDTO | null> => {
      const res = await apiFetch(`/api/medico/nutricao/pacientes/${pacienteId}/rascunho`);
      if (res.status === 204) return null;
      return json<PlanoNutricionalDTO>(res);
    },

    /** Plano vigente (FINALIZADO ativo) do paciente — null se nunca prescrito. */
    buscarVigente: async (pacienteId: string): Promise<PlanoNutricionalDTO | null> => {
      const res = await apiFetch(`/api/medico/nutricao/pacientes/${pacienteId}/vigente`);
      if (res.status === 204) return null;
      return json<PlanoNutricionalDTO>(res);
    },

    listarFinalizadosDoPaciente: (pacienteId: string): Promise<PlanoNutricionalDTO[]> =>
      json<PlanoNutricionalDTO[]>(
        apiFetch(`/api/medico/nutricao/pacientes/${pacienteId}/finalizados`)
      ),

    finalizar: (planoId: string, imagemAssinaturaBase64: string): Promise<PlanoNutricionalDTO> =>
      json<PlanoNutricionalDTO>(
        apiFetch(`/api/medico/nutricao/${planoId}/finalizar`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ imagemAssinaturaBase64 }),
        })
      ),

    /**
     * Cria + finaliza o plano num único POST atômico — sem passar por rascunho.
     * Se a validação falhar, nada é persistido (zero estado parcial no banco).
     */
    finalizarDireto: (input: {
      pacienteId: string;
      tutorId: string;
      parametros: ParametrosDTO;
      cronograma: CronogramaDTO;
      observacoes: string[];
      racaoId?: string | null;
      justificativaDivergencia?: string | null;
      imagemAssinaturaBase64: string;
    }): Promise<PlanoNutricionalDTO> =>
      json<PlanoNutricionalDTO>(
        apiFetch("/api/medico/nutricao/finalizar-direto", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(input),
        })
      ),

    buscarPlano: (planoId: string): Promise<PlanoNutricionalDTO> =>
      json<PlanoNutricionalDTO>(apiFetch(`/api/medico/nutricao/${planoId}`)),
  };
}

// ── Service do tutor ────────────────────────────────────────────────────────

export function criarNutricaoTutorService(apiFetch: ApiFetch) {
  return {
    listarMeusPlanos: (): Promise<PlanoNutricionalDTO[]> =>
      json<PlanoNutricionalDTO[]>(apiFetch("/api/tutor/nutricao")),

    detalhe: (planoId: string): Promise<PlanoNutricionalDTO> =>
      json<PlanoNutricionalDTO>(apiFetch(`/api/tutor/nutricao/${planoId}`)),

    listarCatalogoRacoes: (): Promise<RacaoDTO[]> =>
      json<RacaoDTO[]>(apiFetch("/api/tutor/nutricao/racoes")),
  };
}

export type NutricaoMedicoService = ReturnType<typeof criarNutricaoMedicoService>;
export type NutricaoTutorService = ReturnType<typeof criarNutricaoTutorService>;

/**
 * Formata um valor que pode chegar como number, string (Jackson serializa
 * BigDecimal como string em algumas configurações) ou null. Defensivo —
 * sem isso, `.toFixed()` em string explode a tela inteira.
 */
export function fmt(valor: number | string | null | undefined, casas: number): string {
  if (valor === null || valor === undefined || valor === "") return "—";
  const n = typeof valor === "number" ? valor : Number.parseFloat(valor);
  if (Number.isNaN(n)) return "—";
  return n.toFixed(casas);
}
