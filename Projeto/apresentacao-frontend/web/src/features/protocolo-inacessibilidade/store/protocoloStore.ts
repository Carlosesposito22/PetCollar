import { useSyncExternalStore } from "react";
import type { StatusProtocolo } from "../tipos";

/**
 * Estado global leve da F-03. O projeto não usa Zustand; este módulo replica o
 * essencial (store observável + hook) com {@code useSyncExternalStore}, mantendo
 * os filtros da recepção entre navegações sem acoplar a uma biblioteca externa.
 */
export type FiltrosProtocolos = {
  status: StatusProtocolo[];
  busca: string; // nome/CPF do tutor ou id do paciente/atendimento
  inicio: string; // ISO date "yyyy-MM-dd" ou ""
  fim: string;
};

export const FILTROS_VAZIOS: FiltrosProtocolos = { status: [], busca: "", inicio: "", fim: "" };

type Estado = {
  filtros: FiltrosProtocolos;
};

let estado: Estado = { filtros: FILTROS_VAZIOS };
const ouvintes = new Set<() => void>();

function emitir() {
  for (const ouvinte of ouvintes) ouvinte();
}

function subscrever(ouvinte: () => void): () => void {
  ouvintes.add(ouvinte);
  return () => {
    ouvintes.delete(ouvinte);
  };
}

export const protocoloStore = {
  setFiltros(parcial: Partial<FiltrosProtocolos>) {
    estado = { ...estado, filtros: { ...estado.filtros, ...parcial } };
    emitir();
  },
  limparFiltros() {
    estado = { ...estado, filtros: FILTROS_VAZIOS };
    emitir();
  },
};

/** Hook reativo aos filtros globais da recepção. */
export function useFiltrosProtocolos(): FiltrosProtocolos {
  return useSyncExternalStore(subscrever, () => estado.filtros);
}
