import { useCallback } from "react";
import { ApiError } from "../services/protocoloService";
import { useProtocoloService } from "../services/useProtocoloService";
import { isStatusTerminal, type VisaoProtocoloDTO } from "../tipos";
import { usePolling } from "./usePolling";

/**
 * Polling de 10s da visão do protocolo de um atendimento (tutor, RN 15). O 404 é
 * tratado como "sem protocolo ativo" (dados = null, sem erro). O polling para
 * sozinho quando o protocolo chega a um estado terminal.
 */
export function useProtocoloDoAtendimento(atendimentoId: string) {
  const service = useProtocoloService();

  const loader = useCallback(async (): Promise<VisaoProtocoloDTO | null> => {
    try {
      return await service.visaoDoAtendimento(atendimentoId);
    } catch (e) {
      if (e instanceof ApiError && e.isNaoEncontrado) return null;
      throw e;
    }
  }, [service, atendimentoId]);

  return usePolling<VisaoProtocoloDTO | null>(loader, {
    intervaloMs: 10_000,
    chave: atendimentoId,
    pararQuando: (v) => v != null && isStatusTerminal(v.status),
  });
}
