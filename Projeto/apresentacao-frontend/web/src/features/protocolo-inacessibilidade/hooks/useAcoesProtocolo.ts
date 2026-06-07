import { useCallback, useState } from "react";
import { useProtocoloService } from "../services/useProtocoloService";
import type { ProtocoloDTO } from "../tipos";

/**
 * Ações de escrita do protocolo (recepcionista): ativação manual e encerramento.
 * Expõe estado de execução e propaga o erro do backend (400/409) para a UI tratar.
 */
export function useAcoesProtocolo() {
  const service = useProtocoloService();
  const [executando, setExecutando] = useState(false);

  const ativarManualmente = useCallback(
    async (atendimentoId: string): Promise<ProtocoloDTO> => {
      setExecutando(true);
      try {
        return await service.ativarManualmente(atendimentoId.trim());
      } finally {
        setExecutando(false);
      }
    },
    [service],
  );

  const encerrar = useCallback(
    async (protocoloId: string, detalhes: string): Promise<ProtocoloDTO> => {
      setExecutando(true);
      try {
        return await service.encerrar(protocoloId, detalhes.trim());
      } finally {
        setExecutando(false);
      }
    },
    [service],
  );

  return { ativarManualmente, encerrar, executando };
}
