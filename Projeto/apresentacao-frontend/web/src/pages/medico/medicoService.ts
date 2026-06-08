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

/** Consulta agendada com o médico — vem de GET /api/medico/atendimentos (F-05). */
export type AtendimentoMedicoDTO = {
  consultaId: string;
  pacienteId: string;
  pacienteNome: string;
  tutorNome: string;
  tipo: "INICIAL" | "RETORNO";
  status: string; // StatusConsulta do backend
  inicio: string; // ISO LocalDateTime
  fim: string;
};

/** Forma já normalizada para a tabela do painel do médico. */
export type AtendimentoDoDiaDTO = {
  horario: string; // "dd/MM HH:mm"
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
     * Lista as consultas agendadas com o médico autenticado (hoje em diante).
     * Endpoint real: GET /api/medico/atendimentos (filtra por Principal = medicoId).
     */
    listarAtendimentosDoDia: async (): Promise<AtendimentoDoDiaDTO[]> => {
      const lista = await json<AtendimentoMedicoDTO[]>(apiFetch("/api/medico/atendimentos"));
      return lista.map((a) => ({
        horario: formatarQuando(a.inicio),
        nomePet: a.pacienteNome,
        nomeTutor: a.tutorNome,
        status: mapearStatus(a.status),
        pacienteId: a.pacienteId,
      }));
    },
  };
}

function mapearStatus(status: string): AtendimentoDoDiaDTO["status"] {
  switch (status) {
    case "REALIZADA":
    case "AGUARDANDO_RETORNO":
    case "EXAMES_SOLICITADOS":
      return "CONCLUIDO";
    default: // AGENDADA, CONFIRMADA
      return "AGUARDANDO";
  }
}

function formatarQuando(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString("pt-BR", {
    day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit",
  });
}

export type MedicoService = ReturnType<typeof criarMedicoService>;

// ── Stubs ─────────────────────────────────────────────────────────────────────
// TODO: remover estes stubs quando os endpoints reais forem implementados.

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
