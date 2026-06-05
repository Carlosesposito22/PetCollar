import { useNavigate } from "react-router-dom";
import { ToastProvider } from "./Toast";
import { PainelAgenda } from "./PainelAgenda";

/** Tela de entrada da F-05: ações de agendamento + painel da agenda do tutor. */
export function AgendamentoHub() {
  const navigate = useNavigate();
  return (
    <ToastProvider>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-ink-900">Consultas</h1>
          <p className="text-sm text-ink-500">Agende consultas e retornos e acompanhe sua agenda.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <button onClick={() => navigate("/app/agendamentos/retorno")} className="btn-ghost ring-1 ring-ink-300">
            Agendar retorno
          </button>
          <button onClick={() => navigate("/app/agendamentos/nova-consulta")} className="btn-primary w-auto">
            + Nova consulta
          </button>
        </div>
      </div>

      <PainelAgenda />
    </ToastProvider>
  );
}
