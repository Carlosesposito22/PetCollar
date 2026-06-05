import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../auth/AuthContext";
import { formatarDataHora } from "../../../utils/formato";
import type { Paciente } from "../TutorInicio";
import type { EspecialidadeDTO, HorarioDTO, MedicoDTO } from "./tipos";
import { ApiError } from "./agendamentoService";
import { useAgendamentoService } from "./useAgendamentoService";
import { ToastProvider, useToast } from "./Toast";
import { WizardContainer } from "./WizardContainer";
import { SelecaoPaciente } from "./SelecaoPaciente";
import { CalendarioDisponibilidade } from "./CalendarioDisponibilidade";
import { rotuloMedico, inicialMedico } from "./medico";

const PASSOS = ["Paciente", "Motivo", "Especialidade", "Horário", "Confirmação"];
const MOTIVO_MINIMO = 10;

export function NovaConsultaPage() {
  return (
    <ToastProvider>
      <NovaConsultaInner />
    </ToastProvider>
  );
}

function NovaConsultaInner() {
  const navigate = useNavigate();
  const toast = useToast();
  const api = useAgendamentoService();
  const { session } = useAuth();

  const [passo, setPasso] = useState(0);
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [motivo, setMotivo] = useState("");
  const [especialidade, setEspecialidade] = useState<EspecialidadeDTO | null>(null);
  const [medicoId, setMedicoId] = useState<string | null>(null);
  const [horario, setHorario] = useState<HorarioDTO | null>(null);

  const [avancando, setAvancando] = useState(false);
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null);

  const habilitado = [
    !!paciente,
    motivo.trim().length >= MOTIVO_MINIMO,
    !!especialidade && !!medicoId,
    !!horario,
    !!horario,
  ][passo];

  async function confirmar() {
    if (!paciente || !especialidade || !medicoId || !horario || !session) return;
    setErroConfirmacao(null);
    setAvancando(true);
    try {
      await api.agendarConsultaInicial({
        pacienteId: paciente.id,
        tutorId: session.user.identificador,
        medicoId,
        especialidadeId: especialidade.id,
        motivo: motivo.trim(),
        inicio: horario.inicio,
        fim: horario.fim,
      });
      toast.sucesso("Agendamento confirmado! O médico foi notificado.");
      navigate("/app/agendamentos");
    } catch (e) {
      if (e instanceof ApiError && e.isConflito) {
        setErroConfirmacao(e.message);
      } else {
        toast.erro(e instanceof ApiError ? e.message : "Não foi possível concluir o agendamento.");
      }
    } finally {
      setAvancando(false);
    }
  }

  function avancar() {
    if (passo === PASSOS.length - 1) void confirmar();
    else setPasso(p => p + 1);
  }

  return (
    <WizardContainer
      titulo="Nova consulta"
      subtitulo="Agende uma consulta inicial para um dos seus pacientes."
      passos={PASSOS}
      passoAtual={passo}
      onVoltar={passo > 0 ? () => setPasso(p => p - 1) : undefined}
      onAvancar={avancar}
      avancarHabilitado={habilitado}
      avancando={avancando}
      rotuloAvancar={passo === PASSOS.length - 1 ? "Confirmar agendamento" : "Continuar"}
    >
      {passo === 0 && (
        <SelecaoPaciente
          pacienteSelecionadoId={paciente?.id ?? null}
          onSelecionar={setPaciente}
        />
      )}

      {passo === 1 && (
        <div>
          <label className="label" htmlFor="motivo">Motivo da consulta</label>
          <textarea
            id="motivo" rows={4} className="input"
            placeholder="Descreva brevemente o motivo da consulta…"
            value={motivo} onChange={e => setMotivo(e.target.value)}
          />
          <p className={"mt-1 text-xs " + (motivo.trim().length < MOTIVO_MINIMO ? "text-ink-500" : "text-emerald-600")}>
            {motivo.trim().length}/{MOTIVO_MINIMO} caracteres mínimos.
          </p>
        </div>
      )}

      {passo === 2 && (
        <PassoEspecialidadeMedico
          especialidade={especialidade}
          medicoId={medicoId}
          onEspecialidade={esp => { setEspecialidade(esp); setMedicoId(null); }}
          onMedico={setMedicoId}
        />
      )}

      {passo === 3 && medicoId && (
        <CalendarioDisponibilidade
          medicoId={medicoId}
          horarioSelecionado={horario}
          onSelecionar={setHorario}
        />
      )}

      {passo === 4 && paciente && especialidade && medicoId && horario && (
        <div>
          {erroConfirmacao && (
            <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
              {erroConfirmacao}
            </div>
          )}
          <h3 className="mb-3 text-base font-semibold text-ink-900">Revise os dados</h3>
          <dl className="grid gap-2 text-sm">
            <Resumo rotulo="Paciente" valor={`${paciente.nome} (${paciente.especie})`} />
            <Resumo rotulo="Motivo" valor={motivo.trim()} />
            <Resumo rotulo="Especialidade" valor={especialidade.nome} />
            <Resumo rotulo="Médico" valor={rotuloMedico(medicoId)} />
            <Resumo rotulo="Data e horário" valor={formatarDataHora(horario.inicio)} />
          </dl>
        </div>
      )}
    </WizardContainer>
  );
}

function Resumo({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div className="flex justify-between gap-4 border-b border-ink-300/40 py-2">
      <dt className="text-ink-500">{rotulo}</dt>
      <dd className="text-right font-medium text-ink-800">{valor}</dd>
    </div>
  );
}

function PassoEspecialidadeMedico({
  especialidade, medicoId, onEspecialidade, onMedico,
}: {
  especialidade: EspecialidadeDTO | null;
  medicoId: string | null;
  onEspecialidade: (e: EspecialidadeDTO) => void;
  onMedico: (id: string) => void;
}) {
  const api = useAgendamentoService();
  const [especialidades, setEspecialidades] = useState<EspecialidadeDTO[]>([]);
  const [carregandoEsp, setCarregandoEsp] = useState(true);
  const [medicos, setMedicos] = useState<MedicoDTO[]>([]);
  const [carregandoMed, setCarregandoMed] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let ativo = true;
    api.listarEspecialidades()
      .then(es => { if (ativo) setEspecialidades(es); })
      .catch(e => { if (ativo) setErro((e as Error).message); })
      .finally(() => { if (ativo) setCarregandoEsp(false); });
    return () => { ativo = false; };
  }, [api]);

  useEffect(() => {
    if (!especialidade) { setMedicos([]); return; }
    let ativo = true;
    setCarregandoMed(true);
    api.listarMedicosDaEspecialidade(especialidade.id)
      .then(ms => { if (ativo) setMedicos(ms); })
      .catch(e => { if (ativo) setErro((e as Error).message); })
      .finally(() => { if (ativo) setCarregandoMed(false); });
    return () => { ativo = false; };
  }, [api, especialidade]);

  return (
    <div className="space-y-6">
      {erro && (
        <div role="alert" className="rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">{erro}</div>
      )}

      <section>
        <h3 className="mb-3 text-sm font-semibold text-ink-700">Especialidade</h3>
        {carregandoEsp ? (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map(i => <div key={i} className="h-20 animate-pulse rounded-xl bg-ink-100" />)}
          </div>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {especialidades.map(esp => {
              const sel = esp.id === especialidade?.id;
              return (
                <button key={esp.id} type="button" onClick={() => onEspecialidade(esp)}
                        aria-pressed={sel}
                        className={
                          "card flex items-center gap-3 p-4 text-left transition " +
                          (sel ? "ring-2 ring-brand-500" : "ring-1 ring-black/5 hover:ring-brand-200")
                        }>
                  <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-50 text-lg">🩺</span>
                  <span>
                    <span className="block font-semibold text-ink-900">{esp.nome}</span>
                    {esp.descricao && <span className="block text-xs text-ink-500">{esp.descricao}</span>}
                  </span>
                </button>
              );
            })}
          </div>
        )}
      </section>

      {especialidade && (
        <section>
          <h3 className="mb-3 text-sm font-semibold text-ink-700">Médico</h3>
          {carregandoMed ? (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {[0, 1, 2].map(i => <div key={i} className="h-20 animate-pulse rounded-xl bg-ink-100" />)}
            </div>
          ) : medicos.length === 0 ? (
            <div className="rounded-xl border border-dashed border-ink-300 px-4 py-6 text-center text-sm text-ink-500">
              Nenhum médico disponível nesta especialidade.
            </div>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {medicos.map(m => {
                const sel = m.id === medicoId;
                return (
                  <button key={m.id} type="button" onClick={() => onMedico(m.id)}
                          aria-pressed={sel}
                          className={
                            "card flex items-center gap-3 p-4 text-left transition " +
                            (sel ? "ring-2 ring-brand-500" : "ring-1 ring-black/5 hover:ring-brand-200")
                          }>
                    <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-100 font-bold text-brand-700">
                      {inicialMedico(m.id)}
                    </span>
                    <span>
                      <span className="block font-semibold text-ink-900">{rotuloMedico(m.id)}</span>
                      <span className="block text-xs text-ink-500">{especialidade.nome}</span>
                    </span>
                  </button>
                );
              })}
            </div>
          )}
        </section>
      )}
    </div>
  );
}
