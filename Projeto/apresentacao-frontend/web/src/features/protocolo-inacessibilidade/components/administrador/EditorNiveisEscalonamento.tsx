import {
  CRITICIDADE_DO_NIVEL,
  ROTULO_NIVEL,
  type NivelEscalonamento,
} from "../../tipos";
import { BadgeCriticidade } from "../compartilhados/BadgeCriticidade";

export type EstadoNivel = { nivel: NivelEscalonamento; habilitado: boolean };

type Props = {
  itens: EstadoNivel[];
  onAlternar: (nivel: NivelEscalonamento) => void;
};

/**
 * Editor de níveis de escalonamento (RN 6). A ordem é canônica e fixa (prioridade
 * crescente, exigida pelo domínio), então aqui só se habilita/desabilita cada
 * nível — não há reordenação. A criticidade de cada nível é informativa (RN 9).
 */
export function EditorNiveisEscalonamento({ itens, onAlternar }: Props) {
  return (
    <ul className="space-y-2">
      {itens.map((item, i) => (
        <li
          key={item.nivel}
          className="flex items-center gap-3 rounded-xl border border-ink-300/60 bg-white px-3 py-2"
        >
          <span className="w-6 text-center text-sm font-semibold text-ink-500">{i + 1}</span>
          <span className="flex-1 text-sm font-medium text-ink-800">{ROTULO_NIVEL[item.nivel]}</span>
          <BadgeCriticidade criticidade={CRITICIDADE_DO_NIVEL[item.nivel]} />
          <label className="inline-flex cursor-pointer items-center gap-2 text-xs text-ink-500">
            <input
              type="checkbox"
              checked={item.habilitado}
              onChange={() => onAlternar(item.nivel)}
              className="h-4 w-4 accent-brand-500"
            />
            {item.habilitado ? "Habilitado" : "Desabilitado"}
          </label>
        </li>
      ))}
    </ul>
  );
}
