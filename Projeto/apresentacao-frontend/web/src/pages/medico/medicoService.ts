type ApiFetch = (input: string, init?: RequestInit) => Promise<Response>;

export class ApiError extends Error {
  readonly status: number;
  constructor(status: number, mensagem: string) {
    super(mensagem);
    this.status = status;
    this.name = "ApiError";
  }
}

async function lancarSeErro(res: Response): Promise<Response> {
  if (res.ok) return res;
  const corpo = await res.json().catch(() => ({}) as { mensagem?: string });
  throw new ApiError(res.status, corpo?.mensagem ?? `Falha na requisição (HTTP ${res.status}).`);
}

async function json<T>(entrada: Response | Promise<Response>): Promise<T> {
  return (await lancarSeErro(await entrada)).json() as Promise<T>;
}

// ── Tipos retornados pela API real ────────────────────────────────────────────

/** Item da fila de atendimento — vem de GET /api/recepcao/fila */
export type FilaItemDTO = {
  pacienteId: string;
  triagemId: string;
  corDeRisco: "VERMELHO" | "AMARELO" | "VERDE";
  finalizadaEm: string; // ISO LocalDateTime
};

// ── Tipos stub (sem endpoint implementado ainda) ──────────────────────────────

/**
 * TODO: substituir pelo DTO real quando o endpoint de prontuário for implementado.
 * Endpoint esperado: GET /api/medico/pacientes/:pacienteId/prontuario
 * Funcionalidade relacionada: F-01 (Prontuário Unificado) e F-10 (Relatório Clínico Evolutivo)
 */
export type ProntuarioDTO = {
  pacienteId: string;
  nomePet: string;
  nomeTutor: string;
  especie: string;
  raca: string;
  idadeAnos: number;
  pesoKg: number;
  sexo: string;
  alergias: string[];
  tags: { rotulo: string; alerta: boolean }[];
  triagens: TriagemResumoDTO[];
};

export type TriagemResumoDTO = {
  data: string; // ISO LocalDate
  motivo: string;
  corDeRisco: "VERMELHO" | "AMARELO" | "VERDE";
  pesoTotal: number;
};

/**
 * TODO: substituir pelo DTO real quando o endpoint de agendamentos do médico for implementado.
 * Endpoint esperado: GET /api/medico/atendimentos?data=YYYY-MM-DD
 * Funcionalidade relacionada: F-05 (Agendamento Clínico) — filtro por medicoId ainda não existe
 */
export type AtendimentoDoDiaDTO = {
  horario: string; // HH:mm
  nomePet: string;
  nomeTutor: string;
  status: "AGUARDANDO" | "EM_ATENDIMENTO" | "CONCLUIDO";
  pacienteId: string;
};

// ── Fábrica do serviço ────────────────────────────────────────────────────────

export function criarMedicoService(apiFetch: ApiFetch) {
  return {
    /**
     * Lista a fila de espera dinâmica do consultório.
     * Endpoint real: GET /api/recepcao/fila
     *
     * TODO (consultório): o backend ainda não filtra por consultório do médico.
     * Quando F-02/F-10 estiver integrado, passar ?consultorioId= para filtrar.
     * Por ora retorna toda a fila.
     */
    listarFilaDeEspera: (): Promise<FilaItemDTO[]> =>
      json<FilaItemDTO[]>(apiFetch("/api/recepcao/fila")),

    /**
     * Busca o prontuário completo de um paciente para o médico.
     *
     * TODO: endpoint não implementado. Quando F-10 (Relatório Clínico Evolutivo) for
     * desenvolvido, substituir esta implementação stub pela chamada real:
     *   GET /api/medico/pacientes/:pacienteId/prontuario
     * Remover também os dados hardcoded de PRONTUARIOS_STUB abaixo.
     */
    buscarProntuario: (pacienteId: string): Promise<ProntuarioDTO> => {
      const stub = PRONTUARIOS_STUB[pacienteId] ?? gerarProntuarioStub(pacienteId);
      return Promise.resolve(stub);
    },

    /**
     * Lista os atendimentos agendados do médico para o dia de hoje.
     *
     * TODO: endpoint não implementado. Quando F-05 (Agendamento Clínico) suportar
     * filtro por medicoId + data, substituir pelo endpoint real:
     *   GET /api/medico/atendimentos?data=YYYY-MM-DD
     * O AgendamentoController atual só aceita filtro por pacienteId.
     * Remover também os dados hardcoded de ATENDIMENTOS_STUB abaixo.
     */
    listarAtendimentosDoDia: (): Promise<AtendimentoDoDiaDTO[]> =>
      Promise.resolve(ATENDIMENTOS_STUB),
  };
}

export type MedicoService = ReturnType<typeof criarMedicoService>;

// ── Stubs ─────────────────────────────────────────────────────────────────────
// TODO: remover estes stubs quando os endpoints reais forem implementados.

const ATENDIMENTOS_STUB: AtendimentoDoDiaDTO[] = [
  { horario: "08:00", nomePet: "Rex",  nomeTutor: "Tutor Demo",  status: "CONCLUIDO",      pacienteId: "stub-1" },
  { horario: "09:30", nomePet: "Miau", nomeTutor: "Tutor Demo",  status: "EM_ATENDIMENTO", pacienteId: "stub-2" },
  { horario: "11:00", nomePet: "Bob",  nomeTutor: "Tutor Demo",  status: "AGUARDANDO",     pacienteId: "stub-3" },
  { horario: "14:00", nomePet: "Luna", nomeTutor: "Ana Costa",   status: "AGUARDANDO",     pacienteId: "stub-4" },
  { horario: "15:30", nomePet: "Thor", nomeTutor: "Marcos Silva",status: "AGUARDANDO",     pacienteId: "stub-5" },
];

const PRONTUARIOS_STUB: Record<string, ProntuarioDTO> = {};

function gerarProntuarioStub(pacienteId: string): ProntuarioDTO {
  // TODO: remover quando GET /api/medico/pacientes/:pacienteId/prontuario for implementado
  return {
    pacienteId,
    nomePet: "Paciente",
    nomeTutor: "—",
    especie: "—",
    raca: "—",
    idadeAnos: 0,
    pesoKg: 0,
    sexo: "—",
    alergias: [],
    tags: [],
    triagens: [],
  };
}
