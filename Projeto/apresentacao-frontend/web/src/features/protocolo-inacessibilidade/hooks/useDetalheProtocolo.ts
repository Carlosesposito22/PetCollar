import { useCallback } from "react";
import { useProtocoloService } from "../services/useProtocoloService";
import type {
  DiretivaConsentimentoDTO,
  EventoEscalonamentoDTO,
  NotificacaoProtocoloDTO,
  StatusProtocoloDTO,
  TentativaContatoDTO,
} from "../tipos";
import { usePolling } from "./usePolling";

export type DetalheProtocolo = {
  /** Resumo vindo de /ativos; fica null quando o protocolo deixa de estar ativo (terminal). */
  resumo: StatusProtocoloDTO | null;
  tentativas: TentativaContatoDTO[];
  escalonamentos: EventoEscalonamentoDTO[];
  /** RN 16 — histórico auditável de notificações enviadas durante o protocolo. */
  notificacoes: NotificacaoProtocoloDTO[];
  /** RN 10 — diretivas de consentimento do tutor para o paciente. */
  diretivas: DiretivaConsentimentoDTO[];
};

/**
 * Detalhe completo de um protocolo (recepção), montado a partir dos endpoints
 * disponíveis no backend: resumo em /ativos + /tentativas + /escalonamentos +
 * /notificacoes + /diretivas. Polling de 5s enquanto ativo; para quando o protocolo
 * sai da lista de ativos (estado terminal).
 */
export function useDetalheProtocolo(protocoloId: string) {
  const service = useProtocoloService();

  const loader = useCallback(async (): Promise<DetalheProtocolo> => {
    const [ativos, tentativas, escalonamentos, notificacoes, diretivas] = await Promise.all([
      service.listarAtivos(),
      service.listarTentativas(protocoloId),
      service.listarEscalonamentos(protocoloId),
      service.listarNotificacoes(protocoloId),
      service.listarDiretivas(protocoloId),
    ]);
    return {
      resumo: ativos.find((p) => p.id === protocoloId) ?? null,
      tentativas,
      escalonamentos,
      notificacoes,
      diretivas,
    };
  }, [service, protocoloId]);

  return usePolling<DetalheProtocolo>(loader, {
    intervaloMs: 5_000,
    chave: protocoloId,
    pararQuando: (d) => d.resumo == null,
  });
}
