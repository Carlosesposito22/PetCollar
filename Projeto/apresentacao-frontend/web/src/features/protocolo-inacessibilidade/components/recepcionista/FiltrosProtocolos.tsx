import { ROTULO_STATUS, type StatusProtocolo } from "../../tipos";
import {
  protocoloStore,
  useFiltrosProtocolos,
} from "../../store/protocoloStore";

const STATUS_ATIVOS: StatusProtocolo[] = [
  "ATIVADO",
  "EM_TENTATIVA_TUTOR",
  "EM_TENTATIVA_SECUNDARIOS",
  "EM_ESCALONAMENTO",
];

/**
 * Barra de filtros do painel da recepção. Escreve no store global (persiste entre
 * navegações); a filtragem em si é local à página, pois /ativos não filtra no
 * servidor. A busca casa por id de atendimento, paciente ou protocolo.
 */
export function FiltrosProtocolos() {
  const filtros = useFiltrosProtocolos();

  function alternarStatus(status: StatusProtocolo) {
    const atual = filtros.status;
    const novo = atual.includes(status)
      ? atual.filter((s) => s !== status)
      : [...atual, status];
    protocoloStore.setFiltros({ status: novo });
  }

  const temFiltro =
    filtros.status.length > 0 || filtros.busca !== "" || filtros.inicio !== "" || filtros.fim !== "";

  return (
    <div className="card space-y-4 p-4">
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm font-medium text-ink-700">Status:</span>
        {STATUS_ATIVOS.map((s) => {
          const ativo = filtros.status.includes(s);
          return (
            <button
              key={s}
              onClick={() => alternarStatus(s)}
              aria-pressed={ativo}
              className={
                "rounded-full px-3 py-1 text-xs font-medium ring-1 transition " +
                (ativo
                  ? "bg-brand-500 text-white ring-brand-500"
                  : "bg-white text-ink-700 ring-ink-300 hover:bg-ink-100")
              }
            >
              {ROTULO_STATUS[s]}
            </button>
          );
        })}
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <label className="text-sm">
          <span className="label">Buscar</span>
          <input
            className="input"
            placeholder="ID de atendimento, paciente ou protocolo"
            value={filtros.busca}
            onChange={(e) => protocoloStore.setFiltros({ busca: e.target.value })}
          />
        </label>
        <label className="text-sm">
          <span className="label">Ativado de</span>
          <input
            type="date"
            className="input"
            value={filtros.inicio}
            onChange={(e) => protocoloStore.setFiltros({ inicio: e.target.value })}
          />
        </label>
        <label className="text-sm">
          <span className="label">Ativado até</span>
          <input
            type="date"
            className="input"
            value={filtros.fim}
            onChange={(e) => protocoloStore.setFiltros({ fim: e.target.value })}
          />
        </label>
        <div className="flex items-end">
          <button
            onClick={() => protocoloStore.limparFiltros()}
            disabled={!temFiltro}
            className="btn-ghost ring-1 ring-ink-300 disabled:opacity-50"
          >
            Limpar filtros
          </button>
        </div>
      </div>
    </div>
  );
}
