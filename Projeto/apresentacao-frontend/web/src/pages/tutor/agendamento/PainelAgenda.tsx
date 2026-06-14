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
  RETORNO_AGENDADO: "bg-violet-100 text-violet-800",
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
        <ListaConsultas
          consultas={consultas}
          pacienteNome={pacienteNome}
          especialidades={especialidades}
          onRemarcar={setRemarcando}
          onCancelar={setCancelando}
        />
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

/** Renderiza a lista agrupando pares origem → retorno num único card com seta central. */
function ListaConsultas({
  consultas, pacienteNome, especialidades, onRemarcar, onCancelar,
}: {
  consultas: ConsultaDTO[];
  pacienteNome: Record<string, string>;
  especialidades: Record<string, string>;
  onRemarcar: (c: ConsultaDTO) => void;
  onCancelar: (c: ConsultaDTO) => void;
}) {
  const idsFilhos = new Set(
    consultas
      .filter(c => c.tipo === "RETORNO" && c.consultaOrigemId != null)
      .filter(c => consultas.some(o => o.id === c.consultaOrigemId))
      .map(c => c.id)
  );
  const retornoPorOrigem = new Map(
    consultas
      .filter(c => c.tipo === "RETORNO" && c.consultaOrigemId != null && idsFilhos.has(c.id))
      .map(c => [c.consultaOrigemId as string, c])
  );

  return (
    <div className="space-y-3">
      {consultas
        .filter(c => !idsFilhos.has(c.id))
        .map(c => (
          <CardAgendamento
            key={c.id}
            consulta={c}
            retorno={retornoPorOrigem.get(c.id)}
            pacienteNome={pacienteNome[c.pacienteId] ?? c.pacienteId}
            especialidadeNome={especialidades[c.especialidadeId]}
            retornoPacienteNome={retornoPorOrigem.get(c.id) ? (pacienteNome[retornoPorOrigem.get(c.id)!.pacienteId] ?? retornoPorOrigem.get(c.id)!.pacienteId) : undefined}
            retornoEspecialidadeNome={retornoPorOrigem.get(c.id) ? especialidades[retornoPorOrigem.get(c.id)!.especialidadeId] : undefined}
            onRemarcar={() => onRemarcar(c)}
            onCancelar={() => onCancelar(c)}
            onRemarcarRetorno={retornoPorOrigem.get(c.id) ? () => onRemarcar(retornoPorOrigem.get(c.id)!) : undefined}
            onCancelarRetorno={retornoPorOrigem.get(c.id) ? () => onCancelar(retornoPorOrigem.get(c.id)!) : undefined}
          />
        ))}
    </div>
  );
}

function InfoConsulta({
  consulta, pacienteNome, especialidadeNome,
}: { consulta: ConsultaDTO; pacienteNome: string; especialidadeNome?: string }) {
  const ehRetorno = consulta.tipo === "RETORNO";
  return (
    <div className="min-w-0 flex-1">
      <div className="flex flex-wrap items-center gap-2">
        <span className={
          "inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold " +
          (ehRetorno ? "bg-amber-100 text-amber-800" : "bg-brand-100 text-brand-700")
        }>
          {ROTULO_TIPO[consulta.tipo]}
        </span>
        <span className={"inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold " + STATUS_BADGE[consulta.status]}>
          {ROTULO_STATUS[consulta.status]}
        </span>
      </div>
      <p className="mt-2 text-base font-bold text-ink-900">{formatarDataHora(consulta.inicio)}</p>
      <dl className="mt-1 space-y-0.5 text-sm text-ink-700">
        <Linha rotulo="Paciente" valor={pacienteNome} />
        <Linha rotulo="Médico" valor={consulta.medicoNome} />
        {especialidadeNome && <Linha rotulo="Especialidade" valor={especialidadeNome} />}
        <Linha rotulo="Motivo" valor={consulta.motivo} />
        {consulta.quantidadeRemarcacoes > 0 && (
          <Linha rotulo="Remarcações" valor={String(consulta.quantidadeRemarcacoes)} />
        )}
      </dl>
    </div>
  );
}

function AcoesConsulta({
  consulta, onRemarcar, onCancelar,
}: { consulta: ConsultaDTO; onRemarcar: () => void; onCancelar: () => void }) {
  const ativa = consulta.status === "AGENDADA" || consulta.status === "CONFIRMADA";
  const dentroDe24h = horasAte(consulta.inicio) < ANTECEDENCIA_MINIMA_HORAS;
  if (!ativa) return null;
  return (
    <div className="flex flex-col gap-2 shrink-0">
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
  );
}

function CardAgendamento({
  consulta, retorno, pacienteNome, especialidadeNome,
  retornoPacienteNome, retornoEspecialidadeNome,
  onRemarcar, onCancelar, onRemarcarRetorno, onCancelarRetorno,
}: {
  consulta: ConsultaDTO;
  retorno?: ConsultaDTO;
  pacienteNome: string;
  especialidadeNome?: string;
  retornoPacienteNome?: string;
  retornoEspecialidadeNome?: string;
  onRemarcar: () => void;
  onCancelar: () => void;
  onRemarcarRetorno?: () => void;
  onCancelarRetorno?: () => void;
}) {
  const ehRetornoCard = consulta.tipo === "RETORNO";
  const borderColor = retorno
    ? "border-l-brand-500"
    : ehRetornoCard
    ? "border-l-amber-400"
    : "border-l-brand-400";

  if (retorno && onRemarcarRetorno && onCancelarRetorno) {
    // Card unificado: origem à esquerda, seta central, retorno à direita.
    return (
      <div className={`card border-l-4 p-5 ${borderColor}`}>
        <div className="flex items-start gap-4">
          <InfoConsulta consulta={consulta} pacienteNome={pacienteNome} especialidadeNome={especialidadeNome} />

          {/* Divisor com seta — identidade brand */}
          <div className="flex shrink-0 flex-col items-center self-stretch px-3">
            <div className="w-px flex-1 bg-gradient-to-b from-transparent via-brand-200 to-brand-300" />
            <div className="my-2 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-500 text-white shadow-card">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
                <path fillRule="evenodd" d="M3 10a.75.75 0 0 1 .75-.75h10.638L10.23 5.29a.75.75 0 1 1 1.04-1.08l5.5 5.25a.75.75 0 0 1 0 1.08l-5.5 5.25a.75.75 0 1 1-1.04-1.08l4.158-3.96H3.75A.75.75 0 0 1 3 10Z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="w-px flex-1 bg-gradient-to-b from-brand-300 via-brand-200 to-transparent" />
          </div>

          <InfoConsulta consulta={retorno} pacienteNome={retornoPacienteNome ?? pacienteNome} especialidadeNome={retornoEspecialidadeNome} />
          <AcoesConsulta consulta={retorno} onRemarcar={onRemarcarRetorno} onCancelar={onCancelarRetorno} />
        </div>
      </div>
    );
  }

  return (
    <div className={`card border-l-4 p-5 ${borderColor}`}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <InfoConsulta consulta={consulta} pacienteNome={pacienteNome} especialidadeNome={especialidadeNome} />
        <AcoesConsulta consulta={consulta} onRemarcar={onRemarcar} onCancelar={onCancelar} />
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
          {consulta.medicoNome} · atual: {formatarDataHora(consulta.inicio)}
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
