import { useCallback } from "react";
import { useProtocoloService } from "../services/useProtocoloService";
import type { ConfiguracaoProtocoloDTO } from "../tipos";
import { usePolling } from "./usePolling";

/** Histórico de versões da configuração (admin). Carga única, sem polling. */
export function useHistoricoConfiguracoes() {
  const service = useProtocoloService();
  const loader = useCallback(() => service.historicoConfiguracoes(), [service]);
  return usePolling<ConfiguracaoProtocoloDTO[]>(loader, {
    intervaloMs: 0,
    ativo: false,
    chave: "config-historico",
  });
}
