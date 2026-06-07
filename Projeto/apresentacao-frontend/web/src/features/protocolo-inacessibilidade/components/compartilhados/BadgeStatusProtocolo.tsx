import { COR_STATUS, ROTULO_STATUS, type StatusProtocolo } from "../../tipos";

type Props = {
  status: StatusProtocolo;
  tamanho?: "sm" | "md" | "lg";
};

const TAMANHO: Record<NonNullable<Props["tamanho"]>, string> = {
  sm: "px-2 py-0.5 text-xs",
  md: "px-3 py-1 text-sm",
  lg: "px-4 py-1.5 text-base",
};

/** Badge de status do protocolo — sempre com texto + cor (acessibilidade). */
export function BadgeStatusProtocolo({ status, tamanho = "md" }: Props) {
  return (
    <span
      className={
        "inline-flex items-center gap-1.5 rounded-full font-semibold ring-1 " +
        COR_STATUS[status] +
        " " +
        TAMANHO[tamanho]
      }
    >
      <span aria-hidden className="h-1.5 w-1.5 rounded-full bg-current opacity-70" />
      {ROTULO_STATUS[status]}
    </span>
  );
}
