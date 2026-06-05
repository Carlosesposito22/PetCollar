/** Tipos da F-05 — Agendamento de consultas e retornos (espelham os DTOs do backend). */

export type TipoConsulta = "INICIAL" | "RETORNO";

export type StatusConsulta =
  | "AGENDADA"
  | "CONFIRMADA"
  | "REALIZADA"
  | "CANCELADA"
  | "AGUARDANDO_RETORNO"
  | "EXAMES_SOLICITADOS";

export type StatusExame = "SOLICITADO" | "CONCLUIDO";

export type EspecialidadeDTO = {
  id: string;
  nome: string;
  descricao: string | null;
};

export type MedicoDTO = {
  id: string;
};

export type HorarioDTO = {
  inicio: string; // ISO LocalDateTime
  fim: string;
};

export type HistoricoRemarcacaoDTO = {
  anteriorInicio: string;
  anteriorFim: string;
  novoInicio: string;
  novoFim: string;
  remarcadoEm: string;
};

export type ConsultaDTO = {
  id: string;
  pacienteId: string;
  tutorId: string;
  medicoId: string;
  especialidadeId: string;
  tipo: TipoConsulta;
  motivo: string;
  inicio: string;
  fim: string;
  status: StatusConsulta;
  consultaOrigemId: string | null;
  quantidadeRemarcacoes: number;
  historicoRemarcacoes: HistoricoRemarcacaoDTO[];
};

export type ConsultaElegivelDTO = {
  id: string;
  pacienteId: string;
  medicoId: string;
  especialidadeId: string;
  status: StatusConsulta;
  inicio: string;
};

export type ExameDTO = {
  exameId: string;
  descricao: string;
  status: StatusExame;
};

export type AgendarInicialPayload = {
  pacienteId: string;
  tutorId: string;
  medicoId: string;
  especialidadeId: string;
  motivo: string;
  inicio: string;
  fim: string;
};

export type AgendarRetornoPayload = AgendarInicialPayload & {
  consultaOrigemId: string;
};

export type FiltroAgenda = {
  pacienteId: string;
  status?: StatusConsulta | "";
  tipo?: TipoConsulta | "";
  inicio?: string;
  fim?: string;
};

export const ROTULO_STATUS: Record<StatusConsulta, string> = {
  AGENDADA: "Agendada",
  CONFIRMADA: "Confirmada",
  REALIZADA: "Realizada",
  CANCELADA: "Cancelada",
  AGUARDANDO_RETORNO: "Aguardando retorno",
  EXAMES_SOLICITADOS: "Exames solicitados",
};

export const ROTULO_TIPO: Record<TipoConsulta, string> = {
  INICIAL: "Consulta inicial",
  RETORNO: "Retorno",
};
