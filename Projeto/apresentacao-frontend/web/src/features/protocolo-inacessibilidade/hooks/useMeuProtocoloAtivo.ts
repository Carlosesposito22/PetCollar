import { useCallback } from "react";
import { ApiError } from "../services/protocoloService";
import { useProtocoloService } from "../services/useProtocoloService";
import { isStatusTerminal, type VisaoProtocoloDTO } from "../tipos";
import { usePolling } from "./usePolling";

/**
 * Polling de 10s do protocolo ativo do tutor autenticado (RN 15).
 * Retorna null quando não há protocolo ativo (404 = estado normal).
 * Para o polling automaticamente quando o status é terminal.
 */
export function useMeuProtocoloAtivo() {
  const service = useProtocoloService();

  const loader = useCallback(async (): Promise<VisaoProtocoloDTO | null> => {
    try {
      return await service.meuProtocoloAtivo();
    } catch (e) {
      if (e instanceof ApiError && e.isNaoEncontrado) return null;
      throw e;
    }
  }, [service]);

  return usePolling<VisaoProtocoloDTO | null>(loader, {
    intervaloMs: 10_000,
    chave: "meu-protocolo",
    pararQuando: (v) => v != null && isStatusTerminal(v.status),
  });
}
