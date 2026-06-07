import { useCallback } from "react";
import { useProtocoloService } from "../services/useProtocoloService";
import type { StatusProtocoloDTO } from "../tipos";
import { usePolling } from "./usePolling";

/** Polling de 15s da lista de protocolos ativos (painel da recepção). */
export function useProtocolosAtivos() {
  const service = useProtocoloService();
  const loader = useCallback(() => service.listarAtivos(), [service]);
  return usePolling<StatusProtocoloDTO[]>(loader, { intervaloMs: 15_000, chave: "ativos" });
}
