import { COR_CRITICIDADE, ROTULO_CRITICIDADE, type NivelCriticidade } from "../../tipos";

/** Badge de criticidade da notificação/etapa (RN 9) — texto + cor. */
export function BadgeCriticidade({ criticidade }: { criticidade: NivelCriticidade }) {
  return (
    <span
      className={
        "inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold ring-1 " +
        COR_CRITICIDADE[criticidade]
      }
    >
      {ROTULO_CRITICIDADE[criticidade]}
    </span>
  );
}
