type ApiFetch = (input: string, init?: RequestInit) => Promise<Response>;

export class FarmacoApiError extends Error {
  readonly status: number;
  constructor(status: number, mensagem: string) {
    super(mensagem); this.status = status; this.name = "FarmacoApiError";
  }
}

async function lancarSeErro(res: Response): Promise<Response> {
  if (res.ok) return res;
  const corpo = await res.json().catch(() => ({}) as { mensagem?: string });
  throw new FarmacoApiError(res.status, corpo?.mensagem ?? `Falha na requisição (HTTP ${res.status}).`);
}

async function json<T>(entrada: Response | Promise<Response>): Promise<T> {
  return (await lancarSeErro(await entrada)).json() as Promise<T>;
}

export type Decimal = number | string;

// ── Tipos do catálogo ──────────────────────────────────────────────────────

export type ViaAdministracao =
  | "ORAL" | "SUBCUTANEA" | "INTRAMUSCULAR" | "INTRAVENOSA"
  | "TOPICA" | "OFTALMICA" | "OTOLOGICA";

export type Frequencia =
  | "UMA_VEZ_DIA" | "DUAS_VEZES_DIA" | "TRES_VEZES_DIA"
  | "QUATRO_VEZES_DIA" | "A_CADA_8H" | "A_CADA_12H";

export type ManejoAlimentar = "JEJUM" | "COM_ALIMENTO" | "INDIFERENTE";

export type TagClinica =
  | "GERIATRICO" | "INSUFICIENCIA_RENAL" | "INSUFICIENCIA_HEPATICA" | "CARDIOPATA";

export type MedicamentoDTO = {
  id: string; nome: string;
  doseMaximaMgPorKg: Decimal; concentracaoMgPorMl: Decimal;
  viasPermitidas: ViaAdministracao[];
  componentes: string[];
  manejoAlimentar: ManejoAlimentar;
  notaCuidado: string | null;
};

export type InteracaoDTO = {
  medicamentoAId: string; medicamentoBId: string;
  gravidade: "GRAVE" | "MODERADA" | "LEVE";
  descricao: string;
};

// ── Templates ──────────────────────────────────────────────────────────────

export type TemplateItemDTO = {
  medicamentoId: string; doseMgPorKg: Decimal; duracaoDias: number;
  frequencia: Frequencia; via: ViaAdministracao;
};
export type TemplateDTO = {
  id: string; nome: string; descricao: string; itens: TemplateItemDTO[];
};

// ── Contexto ───────────────────────────────────────────────────────────────

export type ContextoPacienteDTO = {
  pacienteId: string; tutorId: string;
  nomePet: string; nomeTutor: string;
  pesoPacienteKg: Decimal; idadeAnos: number;
  alergiasDoPaciente: string[];
  tagsClinicasDerivadas: TagClinica[];
};

// ── Validação ─────────────────────────────────────────────────────────────

export type RascunhoItem = {
  medicamentoId: string; doseMgPorKg: number; duracaoDias: number;
  frequencia: Frequencia; via: ViaAdministracao;
};

export type ViolacaoDTO = {
  nivel: "BLOQUEIO" | "ALERTA"; codigo: string; mensagem: string;
};

export type DetalheItemDTO = {
  medicamentoId: string;
  doseMaximaSeguraCalculada: Decimal;
  doseTotalPropostaMg: Decimal;
  volumeFinalMl: Decimal;
  tagAplicada: boolean;
  alergiaAplicada: boolean;
};

export type ResultadoValidacaoDTO = {
  podeFinalizar: boolean;
  violacoes: ViolacaoDTO[];
  detalhes: DetalheItemDTO[];
};

// ── Prescrição finalizada ─────────────────────────────────────────────────

export type ItemPrescricaoDTO = {
  medicamentoId: string; nomeMedicamento: string;
  doseMgPorKg: Decimal; doseTotalMg: Decimal; volumeFinalMl: Decimal;
  duracaoDias: number; frequencia: Frequencia; via: ViaAdministracao;
  horarios: string[]; notaCuidado: string | null;
};

export type AssinaturaDTO = {
  medicoResponsavelId: string; imagemBase64: string;
  assinadoEm: string; hashConteudo: string;
};

export type PrescricaoDTO = {
  id: string; pacienteId: string; tutorId: string; medicoResponsavelId: string;
  pesoPacienteKg: Decimal;
  itens: ItemPrescricaoDTO[];
  instrucoesGerais: string[];
  tagsClinicas: TagClinica[];
  alergiasConsideradas: string[];
  status: "FINALIZADA" | "SUBSTITUIDA";
  assinatura: AssinaturaDTO;
  dataInicio: string; dataFim: string;
  criadoEm: string; atualizadoEm: string;
};

export type HistoricoDTO = {
  prescricoes: PrescricaoDTO[];
  nomesDosMedicamentos: Record<string, string>;
};

// ── Rótulos para UI ───────────────────────────────────────────────────────

export const ROTULOS_VIA: Record<ViaAdministracao, string> = {
  ORAL: "Oral", SUBCUTANEA: "Subcutânea", INTRAMUSCULAR: "Intramuscular",
  INTRAVENOSA: "Intravenosa", TOPICA: "Tópica",
  OFTALMICA: "Oftálmica", OTOLOGICA: "Otológica",
};

export const ROTULOS_FREQUENCIA: Record<Frequencia, string> = {
  UMA_VEZ_DIA: "1x ao dia", DUAS_VEZES_DIA: "2x ao dia",
  TRES_VEZES_DIA: "3x ao dia", QUATRO_VEZES_DIA: "4x ao dia",
  A_CADA_8H: "A cada 8h", A_CADA_12H: "A cada 12h",
};

export const ROTULOS_TAG: Record<TagClinica, string> = {
  GERIATRICO: "Geriátrico",
  INSUFICIENCIA_RENAL: "Insuficiência Renal",
  INSUFICIENCIA_HEPATICA: "Insuficiência Hepática",
  CARDIOPATA: "Cardiopata",
};

export const ROTULOS_MANEJO: Record<ManejoAlimentar, string> = {
  JEJUM: "Em jejum", COM_ALIMENTO: "Com alimento", INDIFERENTE: "Indiferente",
};

export const HORARIOS_SUGERIDOS: Record<Frequencia, string[]> = {
  UMA_VEZ_DIA: ["08:00"],
  DUAS_VEZES_DIA: ["08:00", "20:00"],
  TRES_VEZES_DIA: ["08:00", "14:00", "20:00"],
  QUATRO_VEZES_DIA: ["06:00", "12:00", "18:00", "00:00"],
  A_CADA_8H: ["08:00", "16:00", "00:00"],
  A_CADA_12H: ["08:00", "20:00"],
};

export function fmt(v: Decimal | null | undefined, casas: number): string {
  if (v === null || v === undefined || v === "") return "—";
  const n = typeof v === "number" ? v : Number.parseFloat(v);
  if (Number.isNaN(n)) return "—";
  return n.toFixed(casas);
}

// ── Service médico ────────────────────────────────────────────────────────

export function criarFarmacovigilanciaMedicoService(apiFetch: ApiFetch) {
  return {
    contexto: (pacienteId: string): Promise<ContextoPacienteDTO> =>
      json(apiFetch(`/api/medico/farmacovigilancia/pacientes/${pacienteId}/contexto`)),

    catalogo: (): Promise<MedicamentoDTO[]> =>
      json(apiFetch("/api/medico/farmacovigilancia/medicamentos")),

    interacoes: (): Promise<InteracaoDTO[]> =>
      json(apiFetch("/api/medico/farmacovigilancia/medicamentos/interacoes")),

    templates: (): Promise<TemplateDTO[]> =>
      json(apiFetch("/api/medico/farmacovigilancia/templates")),

    validar: (input: {
      pesoPacienteKg: number;
      tagsClinicas: TagClinica[];
      alergias: string[];
      itens: RascunhoItem[];
    }): Promise<ResultadoValidacaoDTO> =>
      json(apiFetch("/api/medico/farmacovigilancia/validar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(input),
      })),

    finalizarDireto: (input: {
      pacienteId: string;
      tutorId: string;
      pesoPacienteKg: number;
      tagsClinicas: TagClinica[];
      alergias: string[];
      itens: Array<RascunhoItem & { horarios: string[]; notaCuidado: string | null }>;
      instrucoesGerais: string[];
      imagemAssinaturaBase64: string;
    }): Promise<PrescricaoDTO> =>
      json(apiFetch("/api/medico/farmacovigilancia/finalizar-direto", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(input),
      })),

    buscarVigente: async (pacienteId: string): Promise<PrescricaoDTO | null> => {
      const res = await apiFetch(`/api/medico/farmacovigilancia/pacientes/${pacienteId}/vigente`);
      if (res.status === 204) return null;
      return json<PrescricaoDTO>(res);
    },

    historico: (pacienteId: string): Promise<HistoricoDTO> =>
      json(apiFetch(`/api/medico/farmacovigilancia/pacientes/${pacienteId}/historico`)),
  };
}

// ── Service tutor ─────────────────────────────────────────────────────────

export function criarFarmacovigilanciaTutorService(apiFetch: ApiFetch) {
  return {
    meusTratamentos: (): Promise<HistoricoDTO> =>
      json(apiFetch("/api/tutor/farmacovigilancia")),

    detalhe: (id: string): Promise<PrescricaoDTO> =>
      json(apiFetch(`/api/tutor/farmacovigilancia/${id}`)),
  };
}

export type FarmacoMedicoService = ReturnType<typeof criarFarmacovigilanciaMedicoService>;
export type FarmacoTutorService = ReturnType<typeof criarFarmacovigilanciaTutorService>;
