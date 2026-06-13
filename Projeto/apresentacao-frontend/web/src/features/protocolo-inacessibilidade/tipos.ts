/**
 * Tipos da F-03 — Protocolo automatizado de tutor inacessível. Espelham os DTOs
 * REST do backend (ver controllers em apresentacao-backend/.../ProtocoloInacessibilidade)
 * e concentram os rótulos/cores/derivações de UI usados pelas três personas.
 */

export type StatusProtocolo =
  | "INATIVO"
  | "ATIVADO"
  | "EM_TENTATIVA_TUTOR"
  | "EM_TENTATIVA_SECUNDARIOS"
  | "EM_ESCALONAMENTO"
  | "ENCERRADO_COM_SUCESSO"
  | "ENCERRADO_POR_ESGOTAMENTO";

export type NivelEscalonamento =
  | "NIVEL_1_ADMINISTRATIVO"
  | "NIVEL_2_COORDENACAO"
  | "NIVEL_3_CLINICO"
  | "NIVEL_4_DIRECAO";

export type CanalContato = "TELEFONE" | "SMS" | "EMAIL" | "WHATSAPP";

export type StatusTentativa = "EXECUTADA_COM_SUCESSO" | "SEM_RESPOSTA" | "FALHA_TECNICA";

export type TipoDestinatario = "TUTOR_PRINCIPAL" | "RESPONSAVEL_SECUNDARIO";

export type NivelCriticidade = "BAIXA" | "MEDIA" | "ALTA" | "CRITICA";

// ── DTOs (espelham os records do backend) ───────────────────────────────────

export type TentativaContatoDTO = {
  id: string;
  destinatarioId: string;
  tipoDestinatario: TipoDestinatario;
  canal: CanalContato;
  status: StatusTentativa;
  executadaEm: string; // ISO LocalDateTime
  mensagemRetorno: string | null;
};

export type EventoEscalonamentoDTO = {
  id: string;
  nivel: NivelEscalonamento;
  motivo: string | null;
  responsavelAcionadoId: string | null;
  ocorridoEm: string;
};

/** GET /api/protocolos/:atendimentoId — visão consolidada para o tutor (RN 15). */
export type VisaoProtocoloDTO = {
  protocoloId: string;
  atendimentoId: string;
  status: StatusProtocolo;
  nivelEscalonamentoAtual: NivelEscalonamento | null;
  ativadoEm: string | null;
  encerradoEm: string | null;
  tentativas: TentativaContatoDTO[];
  eventosEscalonamento: EventoEscalonamentoDTO[];
};

/** GET /api/protocolos/ativos — resumo compacto por protocolo. */
export type StatusProtocoloDTO = {
  id: string;
  atendimentoId: string;
  pacienteId: string;
  status: StatusProtocolo;
  nivelEscalonamentoAtual: NivelEscalonamento | null;
  ativadoEm: string | null;
};

/** Retorno de ativar/encerrar — protocolo completo. */
export type ProtocoloDTO = {
  id: string;
  atendimentoId: string;
  pacienteId: string;
  tutorPrincipalId: string;
  configuracaoId: string;
  status: StatusProtocolo;
  nivelEscalonamentoAtual: NivelEscalonamento | null;
  responsaveisSecundariosAcionados: boolean;
  ativadoEm: string | null;
  encerradoEm: string | null;
  motivoEncerramento: string | null;
  tentativas: TentativaContatoDTO[];
  eventosEscalonamento: EventoEscalonamentoDTO[];
};

export type ConfiguracaoProtocoloDTO = {
  id: string;
  tempoLimiteEsperaMinutos: number;
  canaisHabilitados: CanalContato[];
  intervaloEntreTentativasMinutos: number;
  quantidadeMaximaTentativasPorCanal: number;
  niveisEscalonamento: NivelEscalonamento[];
  versao: number;
  criadaEm: string;
  atualizadaEm: string;
};

export type RequisicaoConfigurarProtocolo = {
  tempoLimiteEsperaMinutos: number;
  canaisHabilitados: CanalContato[];
  intervaloEntreTentativasMinutos: number;
  quantidadeMaximaTentativasPorCanal: number;
  niveisEscalonamento: NivelEscalonamento[];
};

/** GET /api/protocolos/:id/notificacoes — registro auditável de notificação (RN 16). */
export type NotificacaoProtocoloDTO = {
  id: string;
  destinatarioId: string;
  titulo: string;
  corpo: string;
  criticidade: NivelCriticidade;
  registradoEm: string; // ISO LocalDateTime
};

export type TipoConduta =
  | "PROCEDIMENTO_INVASIVO"
  | "MEDICACAO_CONTROLADA"
  | "INTERNACAO"
  | "PROCEDIMENTO_ELETIVO"
  | "EUTANASIA";

/** GET /api/protocolos/:id/diretivas — diretiva de consentimento do tutor (RN 10). */
export type DiretivaConsentimentoDTO = {
  conduta: TipoConduta;
  rotulo: string;
  autorizado: boolean;
};

// ── Rótulos (linguagem onipresente, em pt-BR) ───────────────────────────────

export const ROTULO_STATUS: Record<StatusProtocolo, string> = {
  INATIVO: "Inativo",
  ATIVADO: "Protocolo ativado",
  EM_TENTATIVA_TUTOR: "Em tentativa de contato",
  EM_TENTATIVA_SECUNDARIOS: "Acionando responsáveis secundários",
  EM_ESCALONAMENTO: "Escalonado",
  ENCERRADO_COM_SUCESSO: "Encerrado com sucesso",
  ENCERRADO_POR_ESGOTAMENTO: "Encerrado por esgotamento",
};

export const ROTULO_NIVEL: Record<NivelEscalonamento, string> = {
  NIVEL_1_ADMINISTRATIVO: "Nível 1 — Administrativo",
  NIVEL_2_COORDENACAO: "Nível 2 — Coordenação",
  NIVEL_3_CLINICO: "Nível 3 — Clínico",
  NIVEL_4_DIRECAO: "Nível 4 — Direção",
};

export const ROTULO_CANAL: Record<CanalContato, string> = {
  TELEFONE: "Telefone",
  SMS: "SMS",
  EMAIL: "E-mail",
  WHATSAPP: "WhatsApp",
};

export const ROTULO_STATUS_TENTATIVA: Record<StatusTentativa, string> = {
  EXECUTADA_COM_SUCESSO: "Respondeu",
  SEM_RESPOSTA: "Sem resposta",
  FALHA_TECNICA: "Falha técnica",
};

export const ROTULO_TIPO_DESTINATARIO: Record<TipoDestinatario, string> = {
  TUTOR_PRINCIPAL: "Tutor principal",
  RESPONSAVEL_SECUNDARIO: "Responsável secundário",
};

export const ROTULO_CRITICIDADE: Record<NivelCriticidade, string> = {
  BAIXA: "Baixa",
  MEDIA: "Média",
  ALTA: "Alta",
  CRITICA: "Crítica",
};

// ── Cores (classes Tailwind — badges sempre com texto + cor, a11y) ──────────

export const COR_STATUS: Record<StatusProtocolo, string> = {
  INATIVO: "bg-ink-100 text-ink-700 ring-ink-300",
  ATIVADO: "bg-brand-50 text-brand-700 ring-brand-200",
  EM_TENTATIVA_TUTOR: "bg-amber-50 text-amber-800 ring-amber-200",
  EM_TENTATIVA_SECUNDARIOS: "bg-orange-50 text-orange-800 ring-orange-200",
  EM_ESCALONAMENTO: "bg-paw-50 text-paw-700 ring-paw-200",
  ENCERRADO_COM_SUCESSO: "bg-emerald-50 text-emerald-800 ring-emerald-200",
  ENCERRADO_POR_ESGOTAMENTO: "bg-ink-800/10 text-ink-800 ring-ink-300",
};

export const COR_CRITICIDADE: Record<NivelCriticidade, string> = {
  BAIXA: "bg-ink-100 text-ink-700 ring-ink-300",
  MEDIA: "bg-amber-50 text-amber-800 ring-amber-200",
  ALTA: "bg-orange-50 text-orange-800 ring-orange-200",
  CRITICA: "bg-paw-50 text-paw-700 ring-paw-200",
};

// ── Constantes ordenadas (catálogo completo, p/ editores do admin) ──────────

export const TODOS_CANAIS: CanalContato[] = ["TELEFONE", "SMS", "EMAIL", "WHATSAPP"];

export const TODOS_NIVEIS: NivelEscalonamento[] = [
  "NIVEL_1_ADMINISTRATIVO",
  "NIVEL_2_COORDENACAO",
  "NIVEL_3_CLINICO",
  "NIVEL_4_DIRECAO",
];

// ── Derivações de regra (espelham o domínio; UI só reflete, não decide) ─────

/** Criticidade canônica de cada nível (igual ao enum NivelEscalonamento do backend). */
export const CRITICIDADE_DO_NIVEL: Record<NivelEscalonamento, NivelCriticidade> = {
  NIVEL_1_ADMINISTRATIVO: "ALTA",
  NIVEL_2_COORDENACAO: "ALTA",
  NIVEL_3_CLINICO: "CRITICA",
  NIVEL_4_DIRECAO: "CRITICA",
};

export type Etapa = "TUTOR" | "SECUNDARIOS" | "ESCALONAMENTO";

export const ETAPAS_ORDENADAS: { etapa: Etapa; rotulo: string }[] = [
  { etapa: "TUTOR", rotulo: "Tutor" },
  { etapa: "SECUNDARIOS", rotulo: "Responsáveis secundários" },
  { etapa: "ESCALONAMENTO", rotulo: "Escalonamento" },
];

/** Mapeia o status para a etapa do stepper (RN 4/5 — sequência visual). */
export function etapaDoStatus(status: StatusProtocolo): Etapa | null {
  switch (status) {
    case "ATIVADO":
    case "EM_TENTATIVA_TUTOR":
      return "TUTOR";
    case "EM_TENTATIVA_SECUNDARIOS":
      return "SECUNDARIOS";
    case "EM_ESCALONAMENTO":
      return "ESCALONAMENTO";
    default:
      return null; // INATIVO / encerrados
  }
}

export function isStatusTerminal(status: StatusProtocolo): boolean {
  return status === "ENCERRADO_COM_SUCESSO" || status === "ENCERRADO_POR_ESGOTAMENTO";
}

export function isProtocoloAtivo(status: StatusProtocolo): boolean {
  return status !== "INATIVO" && !isStatusTerminal(status);
}

/** Criticidade "ambiente" do protocolo, p/ tom de banners e cards. */
export function criticidadeDoStatus(
  status: StatusProtocolo,
  nivel: NivelEscalonamento | null,
): NivelCriticidade {
  switch (status) {
    case "EM_ESCALONAMENTO":
      return nivel ? CRITICIDADE_DO_NIVEL[nivel] : "ALTA";
    case "EM_TENTATIVA_SECUNDARIOS":
      return "MEDIA";
    case "EM_TENTATIVA_TUTOR":
    case "ATIVADO":
      return "BAIXA";
    default:
      return "BAIXA";
  }
}

// ── Timeline unificada (tentativas + escalonamentos) ────────────────────────

export type EventoTimeline = {
  id: string;
  tipo: "TENTATIVA" | "ESCALONAMENTO";
  timestamp: string;
  criticidade: NivelCriticidade;
  titulo: string;
  detalhe?: string | null;
};

function criticidadeDaTentativa(t: TentativaContatoDTO): NivelCriticidade {
  if (t.tipoDestinatario === "RESPONSAVEL_SECUNDARIO") return "MEDIA";
  return "BAIXA";
}

/** Funde tentativas e escalonamentos numa linha do tempo (mais recente primeiro). */
export function montarTimeline(
  tentativas: TentativaContatoDTO[],
  eventos: EventoEscalonamentoDTO[],
): EventoTimeline[] {
  const deTentativas: EventoTimeline[] = tentativas.map((t) => ({
    id: t.id,
    tipo: "TENTATIVA",
    timestamp: t.executadaEm,
    criticidade: criticidadeDaTentativa(t),
    titulo: `Tentativa de contato via ${ROTULO_CANAL[t.canal]} — ${ROTULO_STATUS_TENTATIVA[t.status]}`,
    detalhe: t.mensagemRetorno,
  }));
  const deEscalonamentos: EventoTimeline[] = eventos.map((e) => ({
    id: e.id,
    tipo: "ESCALONAMENTO",
    timestamp: e.ocorridoEm,
    criticidade: CRITICIDADE_DO_NIVEL[e.nivel],
    titulo: `Protocolo escalonado para ${ROTULO_NIVEL[e.nivel]}`,
    detalhe: e.motivo,
  }));
  return [...deTentativas, ...deEscalonamentos].sort(
    (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime(),
  );
}

/** Timestamp relativo amigável (ex.: "há 2 minutos"). */
export function tempoRelativo(iso: string | null | undefined): string {
  if (!iso) return "—";
  const data = new Date(iso);
  const ms = Date.now() - data.getTime();
  if (Number.isNaN(ms)) return "—";
  const seg = Math.round(ms / 1000);
  if (seg < 60) return "agora há pouco";
  const min = Math.round(seg / 60);
  if (min < 60) return `há ${min} ${min === 1 ? "minuto" : "minutos"}`;
  const h = Math.round(min / 60);
  if (h < 24) return `há ${h} ${h === 1 ? "hora" : "horas"}`;
  const d = Math.round(h / 24);
  return `há ${d} ${d === 1 ? "dia" : "dias"}`;
}
