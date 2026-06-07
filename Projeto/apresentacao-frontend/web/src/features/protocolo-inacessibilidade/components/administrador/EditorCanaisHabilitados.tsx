import { ROTULO_CANAL, type CanalContato } from "../../tipos";

export type EstadoCanal = { canal: CanalContato; habilitado: boolean };

type Props = {
  itens: EstadoCanal[];
  onMover: (indice: number, direcao: "cima" | "baixo") => void;
  onAlternar: (canal: CanalContato) => void;
};

/**
 * Editor da ordem e habilitação dos canais (RN 2). A ordem da lista define a
 * ordem de tentativa. Sem biblioteca de drag-and-drop: reordenação por botões
 * ↑/↓ (acessível e mobile-friendly).
 */
export function EditorCanaisHabilitados({ itens, onMover, onAlternar }: Props) {
  return (
    <ul className="space-y-2">
      {itens.map((item, i) => (
        <li
          key={item.canal}
          className="flex items-center gap-3 rounded-xl border border-ink-300/60 bg-white px-3 py-2"
        >
          <span className="flex flex-col">
            <button
              onClick={() => onMover(i, "cima")}
              disabled={i === 0}
              aria-label={`Mover ${ROTULO_CANAL[item.canal]} para cima`}
              className="px-1 text-ink-500 disabled:opacity-30"
            >
              ▲
            </button>
            <button
              onClick={() => onMover(i, "baixo")}
              disabled={i === itens.length - 1}
              aria-label={`Mover ${ROTULO_CANAL[item.canal]} para baixo`}
              className="px-1 text-ink-500 disabled:opacity-30"
            >
              ▼
            </button>
          </span>
          <span className="w-6 text-center text-sm font-semibold text-ink-500">{i + 1}</span>
          <span className="flex-1 text-sm font-medium text-ink-800">
            {ROTULO_CANAL[item.canal]}
          </span>
          <label className="inline-flex cursor-pointer items-center gap-2 text-xs text-ink-500">
            <input
              type="checkbox"
              checked={item.habilitado}
              onChange={() => onAlternar(item.canal)}
              className="h-4 w-4 accent-brand-500"
            />
            {item.habilitado ? "Habilitado" : "Desabilitado"}
          </label>
        </li>
      ))}
    </ul>
  );
}
