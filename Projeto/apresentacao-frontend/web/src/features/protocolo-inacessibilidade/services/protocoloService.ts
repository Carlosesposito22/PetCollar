import type {
  ConfiguracaoProtocoloDTO,
  EventoEscalonamentoDTO,
  ProtocoloDTO,
  RequisicaoConfigurarProtocolo,
  StatusProtocoloDTO,
  TentativaContatoDTO,
  VisaoProtocoloDTO,
} from "../tipos";

type ApiFetch = (input: string, init?: RequestInit) => Promise<Response>;

/**
 * Erro de API que carrega o status HTTP, permitindo distinguir 400 (input
 * inválido), 404 (sem protocolo) e 409 (conflito de regra de negócio) na UI.
 */
export class ApiError extends Error {
  readonly status: number;
  constructor(status: number, mensagem: string) {
    super(mensagem);
    this.status = status;
    this.name = "ApiError";
  }
  get isNaoEncontrado() {
    return this.status === 404;
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
 * Fábrica do cliente REST da F-03. Recebe o {@code apiFetch} autenticado do
 * AuthContext e expõe os endpoints realmente implementados no backend.
 *
 * <p>Observações sobre o contrato real:
 * <ul>
 *   <li>não há GET por id de protocolo — o detalhe é montado a partir de
 *       {@link listarAtivos}/{@link listarTentativas}/{@link listarEscalonamentos};</li>
 *   <li>ativação manual recebe apenas o atendimento; encerramento, apenas detalhes
 *       (o backend fixa o motivo como intervenção manual).</li>
 * </ul>
 */
export function criarProtocoloService(apiFetch: ApiFetch) {
  return {
    // ── Visualização ─────────────────────────────────────────────────────────
    /** RN 15 — visão do protocolo ativo de um atendimento (tutor). */
    visaoDoAtendimento: (atendimentoId: string) =>
      json<VisaoProtocoloDTO>(apiFetch(`/api/protocolos/${encodeURIComponent(atendimentoId)}`)),

    listarAtivos: () => json<StatusProtocoloDTO[]>(apiFetch("/api/protocolos/ativos")),

    listarTentativas: (protocoloId: string) =>
      json<TentativaContatoDTO[]>(
        apiFetch(`/api/protocolos/${encodeURIComponent(protocoloId)}/tentativas`),
      ),

    listarEscalonamentos: (protocoloId: string) =>
      json<EventoEscalonamentoDTO[]>(
        apiFetch(`/api/protocolos/${encodeURIComponent(protocoloId)}/escalonamentos`),
      ),

    // ── Ações (recepcionista) ────────────────────────────────────────────────
    ativarManualmente: (atendimentoId: string) =>
      json<ProtocoloDTO>(
        apiFetch("/api/protocolos/ativar-manualmente", {
          method: "POST",
          body: JSON.stringify({ atendimentoId }),
        }),
      ),

    encerrar: (protocoloId: string, detalhes: string) =>
      json<ProtocoloDTO>(
        apiFetch(`/api/protocolos/${encodeURIComponent(protocoloId)}/encerrar`, {
          method: "POST",
          body: JSON.stringify({ detalhes }),
        }),
      ),

    // ── Configuração (administrador) ─────────────────────────────────────────
    configuracaoVigente: () =>
      json<ConfiguracaoProtocoloDTO>(apiFetch("/api/configuracoes-protocolo/vigente")),

    historicoConfiguracoes: () =>
      json<ConfiguracaoProtocoloDTO[]>(apiFetch("/api/configuracoes-protocolo/historico")),

    salvarConfiguracao: (payload: RequisicaoConfigurarProtocolo) =>
      json<ConfiguracaoProtocoloDTO>(
        apiFetch("/api/configuracoes-protocolo", {
          method: "PUT",
          body: JSON.stringify(payload),
        }),
      ),
  };
}

export type ProtocoloService = ReturnType<typeof criarProtocoloService>;
