import { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth } from "../../../auth/AuthContext";
import { formatarDataHora } from "../../../utils/formato";
import type { Paciente } from "../TutorInicio";
import type {
  ConsultaDTO,
  EspecialidadeDTO,
  HorarioDTO,
  StatusConsulta,
  TipoConsulta,
} from "./tipos";
import { ROTULO_STATUS, ROTULO_TIPO } from "./tipos";
import { ApiError } from "./agendamentoService";
import { useAgendamentoService } from "./useAgendamentoService";
import { useToast } from "./Toast";
import { CalendarioDisponibilidade } from "./CalendarioDisponibilidade";
import { rotuloMedico } from "./medico";

const ANTECEDENCIA_MINIMA_HORAS = 24;

function horasAte(inicioIso: string): number {
  return (new Date(inicioIso).getTime() - Date.now()) / 3_600_000;
}

const STATUS_BADGE: Record<StatusConsulta, string> = {
  AGENDADA: "bg-brand-100 text-brand-700",
  CONFIRMADA: "bg-emerald-100 text-emerald-700",
  REALIZADA: "bg-ink-100 text-ink-700",
  CANCELADA: "bg-paw-100 text-paw-700",
  AGUARDANDO_RETORNO: "bg-amber-100 text-amber-800",
  EXAMES_SOLICITADOS: "bg-amber-100 text-amber-800",
};

/** Painel da agenda do tutor (RN 17): filtros + cards diferenciados + ações. */
export function PainelAgenda() {
  const { apiFetch } = useAuth();
  const api = useAgendamentoService();
  const toast = useToast();

  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [pacienteId, setPacienteId] = useState("");
  const [especialidades, setEspecialidades] = useState<Record<string, string>>({});

  const [status, setStatus] = useState<StatusConsulta | "">("");
  const [tipo, setTipo] = useState<TipoConsulta | "">("");
  const [inicio, setInicio] = useState("");
  const [fim, setFim] = useState("");

  const [consultas, setConsultas] = useState<ConsultaDTO[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const [remarcando, setRemarcando] = useState<ConsultaDTO | null>(null);
  const [cancelando, setCancelando] = useState<ConsultaDTO | null>(null);

  const pacienteNome = useMemo(
    () => Object.fromEntries(pacientes.map(p => [p.id, p.nome])),
    [pacientes],
  );

  useEffect(() => {
    apiFetch("/api/tutor/pacientes")
      .then(r => r.json() as Promise<Paciente[]>)
      .then(ps => { setPacientes(ps); if (ps.length > 0) setPacienteId(ps[0].id); })
      .catch(() => setErro("Falha ao carregar pacientes."));
    api.listarEspecialidades()
      .then((es: EspecialidadeDTO[]) =>
        setEspecialidades(Object.fromEntries(es.map(e => [e.id, e.nome]))))
      .catch(() => { /* nomes de especialidade são opcionais no painel */ });
  }, [apiFetch, api]);

  const carregar = useCallback(async () => {
    if (!pacienteId) { setConsultas([]); return; }
    setCarregando(true);
    setErro(null);
    try {
      const lista = await api.listarAgenda({
        pacienteId,
        status: status || undefined,
        tipo: tipo || undefined,
        inicio: inicio ? `${inicio}T00:00:00` : undefined,
        fim: fim ? `${fim}T23:59:59` : undefined,
      });
      setConsultas(lista);
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Falha ao carregar a agenda.");
    } finally {
      setCarregando(false);
    }
  }, [api, pacienteId, status, tipo, inicio, fim]);

  useEffect(() => { void carregar(); }, [carregar]);

  async function cancelar(c: ConsultaDTO) {
    try {
      await api.cancelar(c.id);
      toast.sucesso("Consulta cancelada.");
      setCancelando(null);
      await carregar();
    } catch (e) {
      toast.erro(e instanceof ApiError ? e.message : "Falha ao cancelar.");
    }
  }

  return (
    <div>
      {/* Filtros */}
      <div className="mb-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
        <div>
          <label className="label" htmlFor="f-paciente">Paciente</label>
          <select id="f-paciente" className="input" value={pacienteId} onChange={e => setPacienteId(e.target.value)}>
            {pacientes.length === 0 && <option value="">Nenhum paciente</option>}
            {pacientes.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="f-tipo">Tipo</label>
          <select id="f-tipo" className="input" value={tipo} onChange={e => setTipo(e.target.value as TipoConsulta | "")}>
            <option value="">Todos</option>
            <option value="INICIAL">Consulta inicial</option>
            <option value="RETORNO">Retorno</option>
          </select>
        </div>
        <div>
          <label className="label" htmlFor="f-status">Status</label>
          <select id="f-status" className="input" value={status} onChange={e => setStatus(e.target.value as StatusConsulta | "")}>
            <option value="">Todos</option>
            {(Object.keys(ROTULO_STATUS) as StatusConsulta[]).map(s => (
              <option key={s} value={s}>{ROTULO_STATUS[s]}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="f-inicio">De</label>
          <input id="f-inicio" type="date" className="input" value={inicio} onChange={e => setInicio(e.target.value)} />
        </div>
        <div>
          <label className="label" htmlFor="f-fim">Até</label>
          <input id="f-fim" type="date" className="input" value={fim} onChange={e => setFim(e.target.value)} />
        </div>
      </div>

      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">{erro}</div>
      )}

      {carregando ? (
        <div className="space-y-3">
          {[0, 1].map(i => <div key={i} className="h-28 animate-pulse rounded-2xl bg-white/60" />)}
        </div>
      ) : consultas.length === 0 ? (
        <div className="card px-6 py-12 text-center text-sm text-ink-500">
          Nenhum agendamento encontrado para o filtro selecionado.
        </div>
      ) : (
        <div className="space-y-3">
          {consultas.map(c => (
            <CardAgendamento
              key={c.id}
              consulta={c}
              pacienteNome={pacienteNome[c.pacienteId] ?? c.pacienteId}
              especialidadeNome={especialidades[c.especialidadeId]}
              onRemarcar={() => setRemarcando(c)}
              onCancelar={() => setCancelando(c)}
            />
          ))}
        </div>
      )}

      {remarcando && (
        <RemarcarModal
          consulta={remarcando}
          onFechar={() => setRemarcando(null)}
          onRemarcado={async () => { setRemarcando(null); await carregar(); }}
        />
      )}

      {cancelando && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => setCancelando(null)}>
          <div className="w-full max-w-md card p-6" onClick={e => e.stopPropagation()}>
            <h3 className="mb-3 text-lg font-bold text-ink-900">Cancelar consulta</h3>
            <p className="text-sm text-ink-700">
              Confirmar o cancelamento da consulta de <strong>{formatarDataHora(cancelando.inicio)}</strong>?
            </p>
            <div className="mt-6 flex justify-end gap-2">
              <button onClick={() => setCancelando(null)} className="btn-ghost ring-1 ring-ink-300">Voltar</button>
              <button onClick={() => cancelar(cancelando)} className="btn-primary w-auto bg-paw-500 hover:bg-paw-600">
                Cancelar consulta
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function CardAgendamento({
  consulta, pacienteNome, especialidadeNome, onRemarcar, onCancelar,
}: {
  consulta: ConsultaDTO;
  pacienteNome: string;
  especialidadeNome?: string;
  onRemarcar: () => void;
  onCancelar: () => void;
}) {
  const ativa = consulta.status === "AGENDADA" || consulta.status === "CONFIRMADA";
  const dentroDe24h = horasAte(consulta.inicio) < ANTECEDENCIA_MINIMA_HORAS;
  const retorno = consulta.tipo === "RETORNO";

  return (
    <div className={"card border-l-4 p-5 " + (retorno ? "border-l-amber-400" : "border-l-brand-400")}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <span className={
              "inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold " +
              (retorno ? "bg-amber-100 text-amber-800" : "bg-brand-100 text-brand-700")
            }>
              {ROTULO_TIPO[consulta.tipo]}
            </span>
            <span className={"inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold " + STATUS_BADGE[consulta.status]}>
              {ROTULO_STATUS[consulta.status]}
            </span>
          </div>
          <p className="mt-2 text-lg font-bold text-ink-900">{formatarDataHora(consulta.inicio)}</p>
          <dl className="mt-1 space-y-0.5 text-sm text-ink-700">
            <Linha rotulo="Paciente" valor={pacienteNome} />
            <Linha rotulo="Médico" valor={rotuloMedico(consulta.medicoId)} />
            {especialidadeNome && <Linha rotulo="Especialidade" valor={especialidadeNome} />}
            <Linha rotulo="Motivo" valor={consulta.motivo} />
            {consulta.quantidadeRemarcacoes > 0 && (
              <Linha rotulo="Remarcações" valor={String(consulta.quantidadeRemarcacoes)} />
            )}
          </dl>
        </div>

        {ativa && (
          <div className="flex flex-col gap-2">
            <button onClick={onRemarcar} className="btn-ghost ring-1 ring-ink-300">Remarcar</button>
            <button
              onClick={onCancelar}
              disabled={dentroDe24h}
              title={dentroDe24h ? "Cancelamento exige antecedência mínima de 24 horas." : undefined}
              aria-label={dentroDe24h ? "Cancelamento indisponível: menos de 24 horas de antecedência." : "Cancelar"}
              className="btn-ghost ring-1 ring-paw-200 text-paw-700 hover:bg-paw-50 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Cancelar
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function Linha({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div className="flex gap-1.5">
      <dt className="font-semibold text-ink-800">{rotulo}:</dt>
      <dd className="text-ink-700">{valor}</dd>
    </div>
  );
}

function RemarcarModal({
  consulta, onFechar, onRemarcado,
}: { consulta: ConsultaDTO; onFechar: () => void; onRemarcado: () => void }) {
  const api = useAgendamentoService();
  const toast = useToast();
  const [horario, setHorario] = useState<HorarioDTO | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function confirmar() {
    if (!horario) return;
    setErro(null);
    setEnviando(true);
    try {
      await api.remarcar(consulta.id, horario.inicio, horario.fim);
      toast.sucesso("Consulta remarcada.");
      onRemarcado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Falha ao remarcar.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="w-full max-w-2xl card p-6" onClick={e => e.stopPropagation()}>
        <h3 className="mb-1 text-lg font-bold text-ink-900">Remarcar consulta</h3>
        <p className="mb-4 text-sm text-ink-500">
          {rotuloMedico(consulta.medicoId)} · atual: {formatarDataHora(consulta.inicio)}
        </p>
        {erro && (
          <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">{erro}</div>
        )}
        <CalendarioDisponibilidade
          medicoId={consulta.medicoId}
          horarioSelecionado={horario}
          onSelecionar={setHorario}
        />
        <div className="mt-6 flex justify-end gap-2">
          <button onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
          <button onClick={confirmar} disabled={!horario || enviando} aria-busy={enviando} className="btn-primary w-auto">
            {enviando ? "Remarcando…" : "Confirmar novo horário"}
          </button>
        </div>
      </div>
    </div>
  );
}
