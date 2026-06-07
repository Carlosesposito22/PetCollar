import { type StatusProtocolo } from "../../tipos";

/**
 * Alerta humano e tranquilizador no topo do portal do tutor (RN 11/12). Cor
 * proporcional à criticidade (amarelo em tentativa, laranja nos secundários,
 * vermelho no escalonamento). Sempre reforça a continuidade do atendimento (RN 8).
 */
export function BannerAlertaProtocolo({ status }: { status: StatusProtocolo }) {
  const escalonado = status === "EM_ESCALONAMENTO";
  const secundarios = status === "EM_TENTATIVA_SECUNDARIOS";

  const cor = escalonado
    ? "border-paw-200 bg-paw-50 text-paw-700"
    : secundarios
      ? "border-orange-200 bg-orange-50 text-orange-800"
      : "border-amber-200 bg-amber-50 text-amber-800";

  const titulo = escalonado
    ? "Estamos empenhados em falar com você o quanto antes."
    : "Estamos tentando contato com você sobre o atendimento do seu pet.";

  return (
    <div role="status" className={"rounded-2xl border px-5 py-4 " + cor}>
      <p className="flex items-center gap-2 text-base font-semibold">
        <span aria-hidden>📣</span>
        {titulo}
      </p>
      <p className="mt-1 text-sm opacity-90">
        Por favor, retorne o contato com a clínica assim que puder. Fique tranquilo: o
        atendimento do seu pet continua normalmente enquanto tentamos falar com você.
      </p>
    </div>
  );
}
