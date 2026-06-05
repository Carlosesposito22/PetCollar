import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../auth/AuthContext";
import { formatarDataHora } from "../../../utils/formato";
import type { Paciente } from "../TutorInicio";
import type { ConsultaElegivelDTO, HorarioDTO } from "./tipos";
import { ROTULO_STATUS } from "./tipos";
import { ApiError } from "./agendamentoService";
import { useAgendamentoService } from "./useAgendamentoService";
import { ToastProvider, useToast } from "./Toast";
import { WizardContainer } from "./WizardContainer";
import { SelecaoPaciente } from "./SelecaoPaciente";
import { CalendarioDisponibilidade } from "./CalendarioDisponibilidade";
import { ListaExames } from "./ListaExames";
import { rotuloMedico } from "./medico";

const PASSOS = ["Consulta de origem", "Exames", "Horário", "Confirmação"];
const MOTIVO_RETORNO = "Retorno de acompanhamento";

export function AgendamentoRetornoPage() {
  return (
    <ToastProvider>
      <RetornoInner />
    </ToastProvider>
  );
}

function RetornoInner() {
  const navigate = useNavigate();
  const toast = useToast();
  const api = useAgendamentoService();
  const { session } = useAuth();

  const [passo, setPasso] = useState(0);
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [origem, setOrigem] = useState<ConsultaElegivelDTO | null>(null);
  const [concluidos, setConcluidos] = useState(0);
  const [horario, setHorario] = useState<HorarioDTO | null>(null);

  const [avancando, setAvancando] = useState(false);
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null);

  const onConcluidosChange = useCallback((c: number) => setConcluidos(c), []);

  const habilitado = [!!origem, concluidos >= 1, !!horario, !!horario][passo];

  async function confirmar() {
    if (!paciente || !origem || !horario || !session) return;
    setErroConfirmacao(null);
    setAvancando(true);
    try {
      await api.agendarRetorno({
        pacienteId: paciente.id,
        tutorId: session.user.identificador,
        medicoId: origem.medicoId,
        especialidadeId: origem.especialidadeId,
        motivo: MOTIVO_RETORNO,
        inicio: horario.inicio,
        fim: horario.fim,
        consultaOrigemId: origem.id,
      });
      toast.sucesso("Retorno agendado! O médico foi notificado com os exames vinculados.");
      navigate("/app/agendamentos");
    } catch (e) {
      if (e instanceof ApiError && e.isConflito) setErroConfirmacao(e.message);
      else toast.erro(e instanceof ApiError ? e.message : "Não foi possível agendar o retorno.");
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
      titulo="Agendar retorno"
      subtitulo="Apresente os exames da consulta anterior para liberar o retorno."
      passos={PASSOS}
      passoAtual={passo}
      onVoltar={passo > 0 ? () => setPasso(p => p - 1) : undefined}
      onAvancar={avancar}
      avancarHabilitado={habilitado}
      avancando={avancando}
      rotuloAvancar={passo === PASSOS.length - 1 ? "Confirmar retorno" : "Continuar"}
    >
      {passo === 0 && (
        <PassoOrigem
          paciente={paciente}
          origem={origem}
          onPaciente={p => { setPaciente(p); setOrigem(null); }}
          onOrigem={setOrigem}
        />
      )}

      {passo === 1 && origem && (
        <div>
          {concluidos < 1 && (
            <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
              Conclua pelo menos um exame para prosseguir com o agendamento.
            </div>
          )}
          <ListaExames consultaId={origem.id} onConcluidosChange={onConcluidosChange} />
        </div>
      )}

      {passo === 2 && origem && (
        <div>
          <p className="mb-4 rounded-lg bg-brand-50 px-3 py-2 text-sm text-brand-800 ring-1 ring-brand-100">
            Retorno com <strong>{rotuloMedico(origem.medicoId)}</strong> — o mesmo médico da consulta de origem.
          </p>
          <CalendarioDisponibilidade
            medicoId={origem.medicoId}
            horarioSelecionado={horario}
            onSelecionar={setHorario}
          />
        </div>
      )}

      {passo === 3 && paciente && origem && horario && (
        <div>
          {erroConfirmacao && (
            <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
              {erroConfirmacao}
            </div>
          )}
          <h3 className="mb-3 text-base font-semibold text-ink-900">Revise os dados</h3>
          <dl className="grid gap-2 text-sm">
            <Resumo rotulo="Paciente" valor={`${paciente.nome} (${paciente.especie})`} />
            <Resumo rotulo="Consulta de origem" valor={`${formatarDataHora(origem.inicio)} · ${rotuloMedico(origem.medicoId)}`} />
            <Resumo rotulo="Exames concluídos" valor={String(concluidos)} />
            <Resumo rotulo="Novo horário" valor={formatarDataHora(horario.inicio)} />
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

function PassoOrigem({
  paciente, origem, onPaciente, onOrigem,
}: {
  paciente: Paciente | null;
  origem: ConsultaElegivelDTO | null;
  onPaciente: (p: Paciente) => void;
  onOrigem: (c: ConsultaElegivelDTO) => void;
}) {
  const api = useAgendamentoService();
  const [elegiveis, setElegiveis] = useState<ConsultaElegivelDTO[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!paciente) { setElegiveis([]); return; }
    let ativo = true;
    setCarregando(true);
    setErro(null);
    api.consultasElegiveisRetorno(paciente.id)
      .then(cs => { if (ativo) setElegiveis(cs); })
      .catch(e => { if (ativo) setErro((e as Error).message); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [api, paciente]);

  return (
    <div className="space-y-6">
      <section>
        <h3 className="mb-3 text-sm font-semibold text-ink-700">1. Selecione o paciente</h3>
        <SelecaoPaciente pacienteSelecionadoId={paciente?.id ?? null} onSelecionar={onPaciente} />
      </section>

      {paciente && (
        <section>
          <h3 className="mb-3 text-sm font-semibold text-ink-700">2. Consulta com retorno pendente</h3>
          {carregando ? (
            <div className="space-y-2">
              {[0, 1].map(i => <div key={i} className="h-20 animate-pulse rounded-xl bg-ink-100" />)}
            </div>
          ) : erro ? (
            <div role="alert" className="rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">{erro}</div>
          ) : elegiveis.length === 0 ? (
            <div className="rounded-xl border border-dashed border-ink-300 px-4 py-8 text-center text-sm text-ink-500">
              Nenhuma consulta com retorno pendente para este paciente.
            </div>
          ) : (
            <ul className="space-y-2">
              {elegiveis.map(c => {
                const sel = c.id === origem?.id;
                return (
                  <li key={c.id}>
                    <button type="button" onClick={() => onOrigem(c)} aria-pressed={sel}
                            className={
                              "card flex w-full flex-wrap items-center justify-between gap-3 p-4 text-left transition " +
                              (sel ? "ring-2 ring-brand-500" : "ring-1 ring-black/5 hover:ring-brand-200")
                            }>
                      <div>
                        <p className="font-semibold text-ink-900">{formatarDataHora(c.inicio)}</p>
                        <p className="text-sm text-ink-500">{rotuloMedico(c.medicoId)}</p>
                      </div>
                      <span className="inline-flex rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
                        {ROTULO_STATUS[c.status]}
                      </span>
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </section>
      )}
    </div>
  );
}
