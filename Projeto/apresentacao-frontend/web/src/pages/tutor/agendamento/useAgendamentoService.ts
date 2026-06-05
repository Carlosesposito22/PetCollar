import { useMemo } from "react";
import { useAuth } from "../../../auth/AuthContext";
import { criarAgendamentoService, type AgendamentoService } from "./agendamentoService";

/** Constrói (memoizado) o cliente REST da F-05 a partir do apiFetch autenticado. */
export function useAgendamentoService(): AgendamentoService {
  const { apiFetch } = useAuth();
  return useMemo(() => criarAgendamentoService(apiFetch), [apiFetch]);
}
