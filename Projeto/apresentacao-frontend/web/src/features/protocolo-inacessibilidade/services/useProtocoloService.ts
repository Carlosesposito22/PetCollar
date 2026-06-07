import { useMemo } from "react";
import { useAuth } from "../../../auth/AuthContext";
import { criarProtocoloService, type ProtocoloService } from "./protocoloService";

/** Constrói (memoizado) o cliente REST da F-03 a partir do apiFetch autenticado. */
export function useProtocoloService(): ProtocoloService {
  const { apiFetch } = useAuth();
  return useMemo(() => criarProtocoloService(apiFetch), [apiFetch]);
}
