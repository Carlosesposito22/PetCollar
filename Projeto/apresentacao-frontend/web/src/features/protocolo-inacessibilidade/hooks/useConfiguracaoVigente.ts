import { useCallback } from "react";
import { ApiError } from "../services/protocoloService";
import { useProtocoloService } from "../services/useProtocoloService";
import type { ConfiguracaoProtocoloDTO } from "../tipos";
import { usePolling } from "./usePolling";

/**
 * Configuração vigente (admin). Carga única (sem polling). Se ainda não houver
 * configuração vigente, o backend responde 409; tratamos como "nenhuma vigente"
 * (dados = null) para a UI oferecer a criação da primeira versão.
 */
export function useConfiguracaoVigente() {
  const service = useProtocoloService();
  const loader = useCallback(async (): Promise<ConfiguracaoProtocoloDTO | null> => {
    try {
      return await service.configuracaoVigente();
    } catch (e) {
      if (e instanceof ApiError && (e.isNaoEncontrado || e.isConflito)) return null;
      throw e;
    }
  }, [service]);

  return usePolling<ConfiguracaoProtocoloDTO | null>(loader, {
    intervaloMs: 0,
    ativo: false,
    chave: "config-vigente",
  });
}
