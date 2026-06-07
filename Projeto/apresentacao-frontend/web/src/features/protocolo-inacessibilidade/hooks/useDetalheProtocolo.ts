import { useCallback } from "react";
import { useProtocoloService } from "../services/useProtocoloService";
import type {
  EventoEscalonamentoDTO,
  StatusProtocoloDTO,
  TentativaContatoDTO,
} from "../tipos";
import { usePolling } from "./usePolling";

export type DetalheProtocolo = {
  /** Resumo vindo de /ativos; fica null quando o protocolo deixa de estar ativo (terminal). */
  resumo: StatusProtocoloDTO | null;
  tentativas: TentativaContatoDTO[];
  escalonamentos: EventoEscalonamentoDTO[];
};

/**
 * Detalhe completo de um protocolo (recepção), montado a partir dos endpoints
 * existentes (o backend não expõe GET por id): resumo em /ativos + /tentativas +
 * /escalonamentos. Polling agressivo de 5s enquanto ativo; para quando o
 * protocolo sai da lista de ativos (estado terminal).
 */
export function useDetalheProtocolo(protocoloId: string) {
  const service = useProtocoloService();

  const loader = useCallback(async (): Promise<DetalheProtocolo> => {
    const [ativos, tentativas, escalonamentos] = await Promise.all([
      service.listarAtivos(),
      service.listarTentativas(protocoloId),
      service.listarEscalonamentos(protocoloId),
    ]);
    return {
      resumo: ativos.find((p) => p.id === protocoloId) ?? null,
      tentativas,
      escalonamentos,
    };
  }, [service, protocoloId]);

  return usePolling<DetalheProtocolo>(loader, {
    intervaloMs: 5_000,
    chave: protocoloId,
    pararQuando: (d) => d.resumo == null,
  });
}
