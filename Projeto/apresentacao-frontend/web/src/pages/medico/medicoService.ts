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

// ── Tipos da fila e agenda ────────────────────────────────────────────────────

export type FilaItemDTO = {
  pacienteId: string;
  triagemId: string;
  corDeRisco: "VERMELHO" | "AMARELO" | "VERDE";
  finalizadaEm: string;
  nomePaciente: string;
  tutorId: string;
  medicoId: string | null;
  nomeMedico: string | null;
};

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
  id: string;
  data: string;
  motivo: string;
  corDeRisco: "VERMELHO" | "AMARELO" | "VERDE";
  pesoTotal: number;
};

export type VacinaPendenteDTO = {
  cicloId: string;
  doseId: string;
  ciclo: string;
  rotulo: string;
  doseNumero: number;
  totalDoses: number;
  status: "PENDENTE" | "EM_ATRASO";
  dataAgendada: string;
};

export type AtendimentoMedicoDTO = {
  consultaId: string;
  pacienteId: string;
  pacienteNome: string;
  tutorNome: string;
  tipo: "INICIAL" | "RETORNO";
  status: string;
  inicio: string;
  fim: string;
};

export type AtendimentoDoDiaDTO = {
  horario: string;
  nomePet: string;
  nomeTutor: string;
  status: "AGUARDANDO" | "EM_ATENDIMENTO" | "CONCLUIDO";
  pacienteId: string;
};

// ── Tipos do Relatório Clínico Evolutivo ─────────────────────────────────────

export type TipoRelatorio = "ROTINEIRO" | "CIRURGICO" | "PREVENTIVO";

export type RelatorioDTO = {
  id: string;
  atendimentoId: string;
  pacienteId: string;
  medicoId: string;
  tipoRelatorio: TipoRelatorio;
  diagnosticoTecnico: string | null;
  orientacoesManejo: string | null;
  resumoParaTutor: string | null;
  cuidadosPosOperatorios: string | null;
  tempoRecuperacaoEstimado: string | null;
  medicamentosPrescritos: string[];
  pesoKg: number;
  temperaturaCelsius: number;
  imutavel: boolean;
  criadoEm: string | null;
  assinadoEm: string | null;
};

export type RegistroHistoricoDTO = {
  data: string;
  pesoKg: number;
  temperaturaCelsius: number;
};

export type RequisicaoIniciarRelatorioDTO = {
  atendimentoId: string;
  pacienteId: string;
  medicoId: string;
  tipoRelatorio: TipoRelatorio;
};

export type RequisicaoConteudoDTO = {
  diagnosticoTecnico?: string;
  resumoParaTutor?: string;
  orientacoesManejo?: string;
  cuidadosPosOperatorios?: string;
  tempoRecuperacaoEstimado?: string;
};

// ── Fábrica do serviço ────────────────────────────────────────────────────────

export function criarMedicoService(apiFetch: ApiFetch) {
  return {

    listarFilaDeEspera: (): Promise<FilaItemDTO[]> =>
      json<FilaItemDTO[]>(apiFetch("/api/medico/fila-encaminhada")),

    buscarProntuario: async (pacienteId: string): Promise<ProntuarioDTO> => {
      try {
        return await json<ProntuarioDTO>(apiFetch(`/api/medico/pacientes/${pacienteId}`));
      } catch {
        return PRONTUARIOS_STUB[pacienteId] ?? gerarProntuarioStub(pacienteId);
      }
    },

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

    iniciarRelatorio: (req: RequisicaoIniciarRelatorioDTO): Promise<RelatorioDTO> =>
      json<RelatorioDTO>(
        apiFetch("/api/medico/relatorio/iniciar", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(req),
        })
      ),

    buscarRelatorio: (relatorioId: string): Promise<RelatorioDTO> =>
      json<RelatorioDTO>(apiFetch(`/api/medico/relatorio/${relatorioId}`)),

    registrarSinaisVitais: (
      relatorioId: string,
      pesoKg: number,
      temperaturaCelsius: number
    ): Promise<RelatorioDTO> =>
      json<RelatorioDTO>(
        apiFetch(`/api/medico/relatorio/${relatorioId}/sinais-vitais`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ pesoKg, temperaturaCelsius, frequenciaCardiacaBpm: 80 }),
        })
      ),

    atualizarConteudo: (
      relatorioId: string,
      conteudo: RequisicaoConteudoDTO
    ): Promise<RelatorioDTO> =>
      json<RelatorioDTO>(
        apiFetch(`/api/medico/relatorio/${relatorioId}`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(conteudo),
        })
      ),

    assinarRelatorio: (relatorioId: string): Promise<RelatorioDTO> =>
      json<RelatorioDTO>(
        apiFetch(`/api/medico/relatorio/${relatorioId}/assinar`, { method: "POST" })
      ),

    listarRelatoriosPorPaciente: (pacienteId: string): Promise<RelatorioDTO[]> =>
      json<RelatorioDTO[]>(apiFetch(`/api/medico/relatorio/paciente/${pacienteId}`)),

    buscarHistoricoComparativo: (pacienteId: string): Promise<RegistroHistoricoDTO[]> =>
      json<RegistroHistoricoDTO[]>(
        apiFetch(`/api/medico/relatorio/paciente/${pacienteId}/historico`)
      ),

    atualizarPesoPaciente: async (pacienteId: string, pesoKg: number): Promise<void> => {
      await lancarSeErro(
        await apiFetch(`/api/medico/pacientes/${pacienteId}/peso`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ pesoKg }),
        })
      );
    },

    listarVacinasPendentes: (pacienteId: string): Promise<VacinaPendenteDTO[]> =>
      json<VacinaPendenteDTO[]>(apiFetch(`/api/medico/pacientes/${pacienteId}/vacinas-pendentes`)),

    aplicarVacina: async (
      pacienteId: string, cicloId: string, doseId: string, lote: string
    ): Promise<void> => {
      await lancarSeErro(
        await apiFetch(`/api/medico/pacientes/${pacienteId}/vacinas/aplicar`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ cicloId, doseId, lote }),
        })
      );
    },
  };
}

function mapearStatus(status: string): AtendimentoDoDiaDTO["status"] {
  switch (status) {
    case "REALIZADA":
    case "AGUARDANDO_RETORNO":
    case "EXAMES_SOLICITADOS":
      return "CONCLUIDO";
    default:
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

const PRONTUARIOS_STUB: Record<string, ProntuarioDTO> = {
  "pac-max": {
    pacienteId: "pac-max",
    nomePet: "Max",
    nomeTutor: "Ana Costa",
    especie: "Cão",
    raca: "Labrador",
    idadeAnos: 3,
    pesoKg: 32,
    sexo: "Macho",
    alergias: ["Penicilina", "Dipirona"],
    tags: [
      { rotulo: "Adulto", alerta: false },
      { rotulo: "Porte Grande", alerta: false },
      { rotulo: "Alerta Comportamental", alerta: true },
    ],
    triagens: [
      { id: "stub-max-1", data: "2026-04-28", motivo: "Vômitos frequentes", corDeRisco: "AMARELO", pesoTotal: 5 },
      { id: "stub-max-2", data: "2026-03-15", motivo: "Check-up preventivo", corDeRisco: "VERDE", pesoTotal: 0 },
    ],
  },
  "pac-luna": {
    pacienteId: "pac-luna",
    nomePet: "Luna",
    nomeTutor: "Pedro Souza",
    especie: "Cão",
    raca: "Golden Retriever",
    idadeAnos: 2,
    pesoKg: 28,
    sexo: "Fêmea",
    alergias: [],
    tags: [{ rotulo: "Adulto", alerta: false }],
    triagens: [
      { id: "stub-luna-1", data: "2026-04-28", motivo: "Apatia e recusa alimentar", corDeRisco: "VERMELHO", pesoTotal: 12 },
    ],
  },
};

export const HISTORICO_STUB: RegistroHistoricoDTO[] = [
  { data: "2026-03-15", pesoKg: 32, temperaturaCelsius: 38.5 },
  { data: "2026-02-10", pesoKg: 31.5, temperaturaCelsius: 38.3 },
  { data: "2026-01-05", pesoKg: 30, temperaturaCelsius: 38.4 },
];

export const MEDICAMENTOS_STUB = [
  "Omeprazol 20mg — 1x ao dia por 7 dias",
  "Metoclopramida 10mg — 2x ao dia por 5 dias",
];

function gerarProntuarioStub(pacienteId: string): ProntuarioDTO {
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