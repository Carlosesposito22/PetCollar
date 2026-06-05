import { useEffect, useMemo, useState } from "react";
import { formatarHora } from "../../../utils/formato";
import type { HorarioDTO } from "./tipos";
import { useAgendamentoService } from "./useAgendamentoService";

function inicioDoDia(d: Date) {
  const c = new Date(d);
  c.setHours(0, 0, 0, 0);
  return c;
}

function chaveData(d: Date) {
  // yyyy-MM-dd local
  const ano = d.getFullYear();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
}

const DIAS_SEMANA = ["dom", "seg", "ter", "qua", "qui", "sex", "sáb"];

/**
 * Componente compartilhado (consulta inicial, retorno e remarcação): exibe a grade
 * de horários LIVRES de um médico (RN 4) navegando por semanas. Recebe o médico e o
 * callback de seleção como props — sem nenhuma regra de negócio própria.
 */
export function CalendarioDisponibilidade({
  medicoId,
  horarioSelecionado,
  onSelecionar,
}: {
  medicoId: string;
  horarioSelecionado: HorarioDTO | null;
  onSelecionar: (horario: HorarioDTO) => void;
}) {
  const api = useAgendamentoService();
  const hoje = useMemo(() => inicioDoDia(new Date()), []);
  const [offsetSemana, setOffsetSemana] = useState(0);
  const [diaSelecionado, setDiaSelecionado] = useState<Date>(hoje);
  const [horarios, setHorarios] = useState<HorarioDTO[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const dias = useMemo(() => {
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(hoje);
      d.setDate(hoje.getDate() + offsetSemana * 7 + i);
      return d;
    });
  }, [hoje, offsetSemana]);

  useEffect(() => {
    let ativo = true;
    setCarregando(true);
    setErro(null);
    const inicio = `${chaveData(diaSelecionado)}T00:00:00`;
    const fim = `${chaveData(diaSelecionado)}T23:59:59`;
    api
      .horariosDisponiveis(medicoId, inicio, fim)
      .then(hs => { if (ativo) setHorarios(hs); })
      .catch(e => { if (ativo) setErro((e as Error).message); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [api, medicoId, diaSelecionado]);

  return (
    <div>
      {/* Navegação de semana */}
      <div className="mb-3 flex items-center justify-between">
        <button
          type="button"
          onClick={() => setOffsetSemana(o => Math.max(0, o - 1))}
          disabled={offsetSemana === 0}
          className="btn-ghost ring-1 ring-ink-300 disabled:opacity-40"
        >
          ← Semana anterior
        </button>
        <span className="text-sm font-medium text-ink-700">
          {dias[0].toLocaleDateString("pt-BR", { day: "2-digit", month: "short" })} –{" "}
          {dias[6].toLocaleDateString("pt-BR", { day: "2-digit", month: "short" })}
        </span>
        <button
          type="button"
          onClick={() => setOffsetSemana(o => o + 1)}
          className="btn-ghost ring-1 ring-ink-300"
        >
          Próxima semana →
        </button>
      </div>

      {/* Strip de dias */}
      <div className="mb-5 grid grid-cols-7 gap-1.5">
        {dias.map(d => {
          const selecionado = chaveData(d) === chaveData(diaSelecionado);
          const passado = d < hoje;
          return (
            <button
              key={chaveData(d)}
              type="button"
              disabled={passado}
              onClick={() => setDiaSelecionado(d)}
              className={
                "flex flex-col items-center rounded-xl border px-1 py-2 text-sm transition " +
                (passado
                  ? "cursor-not-allowed border-ink-300/50 text-ink-300"
                  : selecionado
                    ? "border-brand-500 bg-brand-50 font-semibold text-brand-700"
                    : "border-ink-300 text-ink-700 hover:bg-ink-100")
              }
            >
              <span className="text-[11px] uppercase">{DIAS_SEMANA[d.getDay()]}</span>
              <span className="text-base">{d.getDate()}</span>
            </button>
          );
        })}
      </div>

      {/* Grade de horários */}
      {carregando ? (
        <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
          {Array.from({ length: 8 }, (_, i) => (
            <div key={i} className="h-10 animate-pulse rounded-lg bg-ink-100" />
          ))}
        </div>
      ) : erro ? (
        <div role="alert" className="rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
          {erro}
        </div>
      ) : horarios.length === 0 ? (
        <div className="rounded-xl border border-dashed border-ink-300 px-4 py-8 text-center text-sm text-ink-500">
          Nenhum horário livre neste dia. Tente outro dia ou semana.
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
          {horarios.map(h => {
            const selecionado =
              horarioSelecionado?.inicio === h.inicio && horarioSelecionado?.fim === h.fim;
            return (
              <button
                key={h.inicio}
                type="button"
                onClick={() => onSelecionar(h)}
                className={
                  "rounded-lg border px-2 py-2 text-sm font-medium transition " +
                  (selecionado
                    ? "border-brand-500 bg-brand-500 text-white shadow"
                    : "border-brand-200 bg-brand-50 text-brand-700 hover:bg-brand-100")
                }
              >
                {formatarHora(h.inicio)}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
