import { useMemo, useState } from "react";
import {
  ROTULO_CANAL,
  ROTULO_STATUS_TENTATIVA,
  type CanalContato,
  type StatusTentativa,
  type TentativaContatoDTO,
} from "../../tipos";
import { ItemTentativa } from "./ItemTentativa";

type FiltroStatus = StatusTentativa | "TODOS";
type FiltroCanal = CanalContato | "TODOS";

/**
 * Lista de tentativas de contato (RN 2/3), ordenada do mais recente para o mais
 * antigo, com filtros locais por status e canal (sem ida ao servidor — os dados
 * já vêm do `useDetalheProtocolo`).
 */
export function ListaTentativas({ tentativas }: { tentativas: TentativaContatoDTO[] }) {
  const [status, setStatus] = useState<FiltroStatus>("TODOS");
  const [canal, setCanal] = useState<FiltroCanal>("TODOS");

  const filtradas = useMemo(() => {
    return [...tentativas]
      .filter((t) => (status === "TODOS" ? true : t.status === status))
      .filter((t) => (canal === "TODOS" ? true : t.canal === canal))
      .sort((a, b) => new Date(b.executadaEm).getTime() - new Date(a.executadaEm).getTime());
  }, [tentativas, status, canal]);

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-3">
        <label className="text-sm">
          <span className="mr-2 text-ink-500">Status:</span>
          <select
            className="rounded-lg border border-ink-300 bg-white px-2 py-1 text-sm"
            value={status}
            onChange={(e) => setStatus(e.target.value as FiltroStatus)}
          >
            <option value="TODOS">Todos</option>
            {(Object.keys(ROTULO_STATUS_TENTATIVA) as StatusTentativa[]).map((s) => (
              <option key={s} value={s}>
                {ROTULO_STATUS_TENTATIVA[s]}
              </option>
            ))}
          </select>
        </label>
        <label className="text-sm">
          <span className="mr-2 text-ink-500">Canal:</span>
          <select
            className="rounded-lg border border-ink-300 bg-white px-2 py-1 text-sm"
            value={canal}
            onChange={(e) => setCanal(e.target.value as FiltroCanal)}
          >
            <option value="TODOS">Todos</option>
            {(Object.keys(ROTULO_CANAL) as CanalContato[]).map((c) => (
              <option key={c} value={c}>
                {ROTULO_CANAL[c]}
              </option>
            ))}
          </select>
        </label>
      </div>

      {filtradas.length === 0 ? (
        <p className="text-sm text-ink-500">Nenhuma tentativa para os filtros selecionados.</p>
      ) : (
        <div className="space-y-2">
          {filtradas.map((t) => (
            <ItemTentativa key={t.id} tentativa={t} />
          ))}
        </div>
      )}
    </div>
  );
}
