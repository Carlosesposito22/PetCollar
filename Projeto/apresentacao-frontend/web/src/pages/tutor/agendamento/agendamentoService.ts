import type {
  AgendarInicialPayload,
  AgendarRetornoPayload,
  ConsultaDTO,
  ConsultaElegivelDTO,
  EspecialidadeDTO,
  ExameDTO,
  FiltroAgenda,
  HorarioDTO,
  MedicoDTO,
} from "./tipos";

type ApiFetch = (input: string, init?: RequestInit) => Promise<Response>;

/**
 * Erro de API que carrega o status HTTP, permitindo distinguir 400 (input
 * inválido) de 409 (conflito de regra de negócio) na camada de UI.
 */
export class ApiError extends Error {
  readonly status: number;
  constructor(status: number, mensagem: string) {
    super(mensagem);
    this.status = status;
    this.name = "ApiError";
  }
  get isConflito() {
    return this.status === 409;
  }
}

async function lancarSeErro(entrada: Response | Promise<Response>): Promise<Response> {
  const res = await entrada;
  if (res.ok) return res;
  const corpo = await res.json().catch(() => ({}) as { mensagem?: string });
  throw new ApiError(res.status, corpo?.mensagem ?? `Falha na requisição (HTTP ${res.status}).`);
}

async function json<T>(entrada: Response | Promise<Response>): Promise<T> {
  return (await lancarSeErro(entrada)).json() as Promise<T>;
}

/**
 * Fábrica do cliente REST da F-05. Recebe o {@code apiFetch} autenticado do
 * AuthContext e expõe todos os endpoints do backend de agendamento.
 */
export function criarAgendamentoService(apiFetch: ApiFetch) {
  return {
    // ── Especialidades e médicos ────────────────────────────────────────────
    listarEspecialidades: () =>
      json<EspecialidadeDTO[]>(apiFetch("/api/especialidades")),

    listarMedicosDaEspecialidade: (especialidadeId: string) =>
      json<MedicoDTO[]>(apiFetch(`/api/especialidades/${especialidadeId}/medicos`)),

    // ── Disponibilidade ─────────────────────────────────────────────────────
    horariosDisponiveis: (medicoId: string, inicio: string, fim: string) => {
      const params = new URLSearchParams({ inicio, fim });
      return json<HorarioDTO[]>(
        apiFetch(`/api/medicos/${medicoId}/horarios-disponiveis?${params.toString()}`),
      );
    },

    // ── Agendamentos ────────────────────────────────────────────────────────
    agendarConsultaInicial: (payload: AgendarInicialPayload) =>
      json<ConsultaDTO>(
        apiFetch("/api/agendamentos/consulta-inicial", {
          method: "POST",
          body: JSON.stringify(payload),
        }),
      ),

    agendarRetorno: (payload: AgendarRetornoPayload) =>
      json<ConsultaDTO>(
        apiFetch("/api/agendamentos/retorno", {
          method: "POST",
          body: JSON.stringify(payload),
        }),
      ),

    remarcar: (id: string, inicio: string, fim: string) =>
      json<ConsultaDTO>(
        apiFetch(`/api/agendamentos/${id}/remarcar`, {
          method: "PUT",
          body: JSON.stringify({ inicio, fim }),
        }),
      ),

    cancelar: async (id: string) => {
      await lancarSeErro(await apiFetch(`/api/agendamentos/${id}`, { method: "DELETE" }));
    },

    listarAgenda: (filtro: FiltroAgenda) => {
      const params = new URLSearchParams({ pacienteId: filtro.pacienteId });
      if (filtro.status) params.set("status", filtro.status);
      if (filtro.tipo) params.set("tipo", filtro.tipo);
      if (filtro.inicio) params.set("inicio", filtro.inicio);
      if (filtro.fim) params.set("fim", filtro.fim);
      return json<ConsultaDTO[]>(apiFetch(`/api/agendamentos?${params.toString()}`));
    },

    // ── Retorno: consultas elegíveis e exames ───────────────────────────────
    consultasElegiveisRetorno: (pacienteId: string) =>
      json<ConsultaElegivelDTO[]>(
        apiFetch(`/api/pacientes/${pacienteId}/consultas-elegiveis-retorno`),
      ),

    examesSolicitados: (consultaId: string) =>
      json<ExameDTO[]>(apiFetch(`/api/consultas/${consultaId}/exames-solicitados`)),

    confirmarExame: async (consultaId: string, exameId: string) => {
      await lancarSeErro(
        await apiFetch(`/api/consultas/${consultaId}/exames/${exameId}/confirmar`, {
          method: "POST",
        }),
      );
    },

    registrarLaudo: async (consultaId: string, exameId: string, laudo: string) => {
      await lancarSeErro(
        await apiFetch(`/api/consultas/${consultaId}/exames/${exameId}/laudo`, {
          method: "POST",
          body: JSON.stringify({ laudo }),
        }),
      );
    },
  };
}

export type AgendamentoService = ReturnType<typeof criarAgendamentoService>;
