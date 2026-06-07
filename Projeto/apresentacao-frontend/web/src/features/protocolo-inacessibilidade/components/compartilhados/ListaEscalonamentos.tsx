import { useMemo } from "react";
import type { EventoEscalonamentoDTO } from "../../tipos";
import { ItemEscalonamento } from "./ItemEscalonamento";

/** Lista de eventos de escalonamento (RN 6/7), em ordem cronológica decrescente. */
export function ListaEscalonamentos({ eventos }: { eventos: EventoEscalonamentoDTO[] }) {
  const ordenados = useMemo(
    () =>
      [...eventos].sort(
        (a, b) => new Date(b.ocorridoEm).getTime() - new Date(a.ocorridoEm).getTime(),
      ),
    [eventos],
  );

  if (ordenados.length === 0) {
    return <p className="text-sm text-ink-500">Este protocolo ainda não foi escalonado.</p>;
  }

  return (
    <div className="space-y-2">
      {ordenados.map((e) => (
        <ItemEscalonamento key={e.id} evento={e} />
      ))}
    </div>
  );
}
