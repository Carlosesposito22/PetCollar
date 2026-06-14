import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import {
  criarMedicoService,
  type MedicoService,
  type ProntuarioDTO,
  type VacinaAplicadaDTO,
  type VacinaPendenteDTO,
} from "./medicoService";
import { gerarPdfRelatorio, listarRelatorios, type RelatorioSalvo } from "./relatorioStorage";

export function MedicoProntuario() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const navigate = useNavigate();
  const { apiFetch } = useAuth();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  const [prontuario, setProntuario] = useState<ProntuarioDTO | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [relatorios, setRelatorios] = useState<RelatorioSalvo[]>([]);
  const [vacinasAplicadas, setVacinasAplicadas] = useState<VacinaAplicadaDTO[]>([]);
  const [vacinasPendentes, setVacinasPendentes] = useState<VacinaPendenteDTO[]>([]);
  const [modalHistVacina, setModalHistVacina] = useState(false);
  const [modalFinalizar, setModalFinalizar] = useState(false);

  useEffect(() => {
    if (!pacienteId) return;
    setRelatorios(listarRelatorios(pacienteId));
    service
      .buscarProntuario(pacienteId)
      .then(setProntuario)
      .catch((e: Error) => setErro(e.message))
      .finally(() => setCarregando(false));
    // Vacinas (para indicar atendimentos de vacinação, o histórico e o estado do botão).
    service.listarVacinasAplicadas(pacienteId).then(setVacinasAplicadas).catch(() => setVacinasAplicadas([]));
    service.listarVacinasPendentes(pacienteId).then(setVacinasPendentes).catch(() => setVacinasPendentes([]));
  }, [service, pacienteId]);

  if (carregando) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded-xl bg-ink-100" />
        <div className="card h-40 animate-pulse" />
        <div className="card h-60 animate-pulse" />
      </div>
    );
  }

  if (erro || !prontuario) {
    return (
      <div>
        <button onClick={() => navigate("/medico")} className="btn-ghost mb-4 text-sm">
          ← Voltar ao Painel
        </button>
        <div
          role="alert"
          className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900"
        >
          {erro ?? "Prontuário não encontrado."}
        </div>
      </div>
    );
  }

  // O modo do atendimento vem da triagem mais recente: aplicação de vacina libera
  // só a seção Vacinação; atendimento clínico libera as demais (e bloqueia Vacinação).
  const ehVacina = !!prontuario.triagens[0]?.aplicacaoVacina;
  // A seção Vacinação só fica ativa enquanto há dose pendente; após aplicar tudo, desabilita.
  const temVacinaPendente = vacinasPendentes.length > 0;

  return (
    <div className="space-y-6">
      {/* Navegação de volta */}
      <div className="flex items-center justify-between">
        <button onClick={() => navigate("/medico")} className="btn-ghost text-sm">
          ← Voltar ao Painel
        </button>
        <h1 className="text-xl font-bold text-ink-900">Prontuário do Paciente</h1>
        <div className="w-32" /> {/* espaçador para centralizar o título */}
      </div>

      {/* ── Seção 1: Alerta de Alergias (condicional) ──────────────────────── */}
      {prontuario.alergias.length > 0 && (
        <div
          role="alert"
          className="rounded-2xl border-2 border-red-300 bg-red-50 p-5"
        >
          <div className="flex items-center gap-2 mb-3">
            <span className="text-xl">⚠️</span>
            <h2 className="font-bold text-red-800 text-base">Alergias Conhecidas</h2>
          </div>
          <p className="text-sm text-red-700">
            {prontuario.alergias.map((a, i) => (
              <span key={i}>
                {i > 0 && <span className="mx-2 text-red-400">|</span>}
                • {a}
              </span>
            ))}
          </p>
        </div>
      )}

      {/* ── Seção 2: Dados do Paciente ─────────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Dados do Paciente</h2>

        {/* TODO: todos os campos abaixo vêm do stub até que
            GET /api/medico/pacientes/:pacienteId/prontuario seja implementado (F-10) */}
        <dl className="grid grid-cols-2 gap-x-8 gap-y-3 sm:grid-cols-3">
          <CampoInfo rotulo="Nome" valor={prontuario.nomePet} />
          <CampoInfo rotulo="Espécie" valor={prontuario.especie} />
          <CampoInfo rotulo="Raça" valor={prontuario.raca} />
          <CampoInfo
            rotulo="Idade"
            valor={prontuario.idadeAnos > 0 ? `${prontuario.idadeAnos} anos` : "—"}
          />
          <CampoInfo
            rotulo="Peso"
            valor={prontuario.pesoKg > 0 ? `${prontuario.pesoKg} kg` : "—"}
          />
          <CampoInfo rotulo="Sexo" valor={prontuario.sexo} />
        </dl>

        {/* Tags de perfil */}
        {prontuario.tags.length > 0 && (
          <div className="mt-5 flex flex-wrap gap-2">
            {prontuario.tags.map((tag, i) => (
              <span
                key={i}
                className={
                  "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1 " +
                  (tag.alerta
                    ? "bg-amber-50 text-amber-800 ring-amber-300"
                    : "bg-brand-50 text-brand-700 ring-brand-100")
                }
              >
                {tag.rotulo}
              </span>
            ))}
          </div>
        )}
      </div>

      {/* ── Seção 3: Histórico de Triagens ────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Histórico de Triagens</h2>

        {/* TODO: triagens vêm do stub até que endpoint de prontuário seja implementado (F-01 / F-10) */}
        {prontuario.triagens.length === 0 ? (
          <p className="text-sm text-ink-500">Nenhuma triagem registrada para este paciente.</p>
        ) : (
          <div className="space-y-3">
            {prontuario.triagens.map((t, i) => (
              <TriagemItem
                key={t.id ?? i}
                triagem={t}
                relatorio={relatorios.find((r) => r.triagemId === t.id) ?? null}
                onBaixarRelatorio={(r) => gerarPdfRelatorio(r)}
                onEmitirRelatorio={() =>
                  navigate(`/medico/prontuario/${pacienteId}/relatorio?triagem=${t.id}`)
                }
                onVerHistoricoVacinacao={() => setModalHistVacina(true)}
              />
            ))}
          </div>
        )}
      </div>

      {/* ── Seção 4: Ações do Prontuário ──────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-1 text-base font-semibold text-ink-900">Acesso às Seções</h2>
        <p className="mb-4 text-xs text-ink-500">
          {ehVacina
            ? "Atendimento de aplicação de vacina — apenas a seção Vacinação está habilitada."
            : "Atendimento clínico — a seção Vacinação fica habilitada apenas em atendimentos de vacina."}
        </p>
        <div className="grid grid-cols-2 gap-3">
          <BotaoAcao
            titulo="Relatório Clínico"
            disabled={ehVacina}
            motivoDesabilitado="Indisponível em atendimento de aplicação de vacina."
            onClick={() => {
              const ultima = prontuario.triagens[0];
              if (!ultima) {
                alert("Nenhum atendimento (triagem) registrado. O relatório é emitido por atendimento.");
                return;
              }
              navigate(`/medico/prontuario/${pacienteId}/relatorio?triagem=${ultima.id}`);
            }}
            destaque
          />
          <BotaoAcao
            titulo="Central de Farmacovigilância e Prescrição"
            disabled={ehVacina}
            motivoDesabilitado="Indisponível em atendimento de aplicação de vacina."
            onClick={() => navigate(`/medico/prontuario/${pacienteId}/farmacovigilancia`)}
          />
          <BotaoAcao
            titulo="Gestão Nutricional"
            disabled={ehVacina}
            motivoDesabilitado="Indisponível em atendimento de aplicação de vacina."
            onClick={() => navigate(`/medico/prontuario/${pacienteId}/nutricao`)}
          />
          <BotaoAcao
            titulo={ehVacina && !temVacinaPendente ? "Vacinação (concluída)" : "Vacinação"}
            disabled={!ehVacina || !temVacinaPendente}
            motivoDesabilitado={
              !ehVacina
                ? "Disponível apenas em atendimentos de aplicação de vacina."
                : "Todas as vacinas pendentes já foram aplicadas."
            }
            onClick={() => navigate(`/medico/prontuario/${pacienteId}/vacinacao`)}
          />
        </div>
      </div>

      {/* ── Seção 5: Finalizar Atendimento ────────────────────────────────── */}
      <div className="flex items-center justify-between rounded-2xl border border-ink-200 bg-white px-6 py-4">
        <div>
          <p className="text-sm font-semibold text-ink-900">Finalizar Atendimento</p>
          <p className="text-xs text-ink-500">Encerra o atendimento e remove o paciente da fila.</p>
        </div>
        <button
          type="button"
          onClick={() => setModalFinalizar(true)}
          className="btn-primary w-auto px-5 py-2.5 text-sm"
        >
          Finalizar
        </button>
      </div>

      {modalHistVacina && (
        <ModalHistoricoVacinacao
          vacinas={vacinasAplicadas}
          onFechar={() => setModalHistVacina(false)}
        />
      )}

      {modalFinalizar && pacienteId && (
        <ModalFinalizarAtendimento
          service={service}
          pacienteId={pacienteId}
          onFechado={() => setModalFinalizar(false)}
          onFinalizado={() => navigate("/medico")}
        />
      )}
    </div>
  );
}

// ── Subcomponentes ─────────────────────────────────────────────────────────────

const EXAMES_PREDEFINIDOS_PRONT = [
  "Hemograma completo",
  "Bioquímico sérico",
  "Urinálise (EAS)",
  "Radiografia",
  "Ultrassonografia abdominal",
  "Cultura e antibiograma",
  "PCR / Teste sorológico",
  "Exame parasitológico de fezes",
];

type EtapaPront = "retorno" | "tipo" | "exames";

function ModalFinalizarAtendimento({
  service, pacienteId, onFechado, onFinalizado,
}: {
  service: MedicoService;
  pacienteId: string;
  onFechado: () => void;
  onFinalizado: () => void;
}) {
  const [etapa, setEtapa] = useState<EtapaPront>("retorno");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [examesSelecionados, setExamesSelecionados] = useState<Set<string>>(new Set());
  const [examesExtras, setExamesExtras] = useState<string[]>([]);
  const [novoExame, setNovoExame] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  function toggleExame(nome: string) {
    setExamesSelecionados(prev => {
      const next = new Set(prev);
      next.has(nome) ? next.delete(nome) : next.add(nome);
      return next;
    });
  }

  function adicionarExameExtra() {
    const trim = novoExame.trim();
    if (trim && !examesSelecionados.has(trim) && !examesExtras.includes(trim)) {
      setExamesExtras(prev => [...prev, trim]);
      setExamesSelecionados(prev => new Set([...prev, trim]));
    }
    setNovoExame("");
    inputRef.current?.focus();
  }

  function listaFinal() {
    return [...examesSelecionados];
  }

  async function concluir(temRetorno: boolean, comExames: boolean, exames: string[]) {
    setEnviando(true);
    setErro(null);
    try {
      if (temRetorno) {
        await service.liberarRetorno(pacienteId, comExames, exames);
      }
      await service.finalizarAtendimento(pacienteId);
      onFinalizado();
    } catch (e) {
      setErro((e as Error).message || "Não foi possível finalizar o atendimento.");
      setEnviando(false);
    }
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(e) => { if (e.target === e.currentTarget && !enviando) onFechado(); }}
    >
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 className="text-lg font-bold text-ink-900">Finalizar Atendimento</h2>

        {erro && (
          <div role="alert" className="mt-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-800">
            {erro}
          </div>
        )}

        {/* Etapa 1: retorno? */}
        {etapa === "retorno" && (
          <>
            <p className="mt-4 text-sm font-semibold text-ink-800">Esta consulta dá direito a retorno?</p>
            <div className="mt-3 flex flex-col gap-3">
              <button disabled={enviando} onClick={() => concluir(false, false, [])}
                className="w-full rounded-xl border border-ink-200 bg-ink-50 px-4 py-3 text-left text-sm transition hover:bg-ink-100 disabled:opacity-50">
                <span className="block font-semibold text-ink-900">Não — encerrar normalmente</span>
                <span className="text-xs text-ink-500">Atendimento concluído sem elegibilidade de retorno.</span>
              </button>
              <button disabled={enviando} onClick={() => setEtapa("tipo")}
                className="w-full rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-left text-sm transition hover:bg-brand-100 disabled:opacity-50">
                <span className="block font-semibold text-brand-900">Sim — liberar retorno</span>
                <span className="text-xs text-brand-700">O tutor poderá agendar retorno pela plataforma.</span>
              </button>
            </div>
            <button onClick={onFechado} disabled={enviando}
              className="mt-4 w-full rounded-xl border border-ink-200 py-2 text-sm text-ink-600 transition hover:bg-ink-50 disabled:opacity-50">
              Cancelar
            </button>
          </>
        )}

        {/* Etapa 2: tipo de retorno */}
        {etapa === "tipo" && (
          <>
            <p className="mt-4 text-sm font-semibold text-ink-800">Tipo de retorno:</p>
            <div className="mt-3 flex flex-col gap-3">
              <button disabled={enviando} onClick={() => concluir(true, false, [])}
                className="w-full rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-left text-sm transition hover:bg-brand-100 disabled:opacity-50">
                <span className="block font-semibold text-brand-900">Retorno simples</span>
                <span className="text-xs text-brand-700">Tutor agenda diretamente, sem etapa de exames.</span>
              </button>
              <button disabled={enviando} onClick={() => setEtapa("exames")}
                className="w-full rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-left text-sm transition hover:bg-amber-100 disabled:opacity-50">
                <span className="block font-semibold text-amber-900">Com exames pendentes</span>
                <span className="text-xs text-amber-700">Defina os exames que o tutor precisará confirmar.</span>
              </button>
            </div>
            <button onClick={() => setEtapa("retorno")} disabled={enviando}
              className="mt-4 text-xs text-ink-400 underline hover:text-ink-600 disabled:opacity-50">
              ← Voltar
            </button>
          </>
        )}

        {/* Etapa 3: selecionar exames */}
        {etapa === "exames" && (
          <>
            <p className="mt-4 text-sm font-semibold text-ink-800">
              Exames solicitados para o retorno:
            </p>
            <p className="mt-0.5 text-xs text-ink-500">
              O tutor verá esta lista e precisará confirmar cada item antes de agendar.
            </p>
            <ul className="mt-3 max-h-48 space-y-1.5 overflow-y-auto pr-1">
              {[...EXAMES_PREDEFINIDOS_PRONT, ...examesExtras].map(nome => (
                <li key={nome}>
                  <label className="flex cursor-pointer items-center gap-3 rounded-lg border border-ink-200 px-3 py-2 text-sm transition hover:bg-ink-50 has-[:checked]:border-brand-300 has-[:checked]:bg-brand-50">
                    <input
                      type="checkbox"
                      className="accent-brand-600"
                      checked={examesSelecionados.has(nome)}
                      onChange={() => toggleExame(nome)}
                    />
                    <span className="text-ink-800">{nome}</span>
                  </label>
                </li>
              ))}
            </ul>
            <div className="mt-3 flex gap-2">
              <input
                ref={inputRef}
                type="text"
                placeholder="Adicionar exame personalizado…"
                value={novoExame}
                onChange={e => setNovoExame(e.target.value)}
                onKeyDown={e => { if (e.key === "Enter") { e.preventDefault(); adicionarExameExtra(); }}}
                className="flex-1 rounded-lg border border-ink-300 px-3 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
              />
              <button type="button" onClick={adicionarExameExtra} disabled={!novoExame.trim()}
                className="rounded-lg border border-brand-300 bg-brand-50 px-3 py-1.5 text-sm font-medium text-brand-700 hover:bg-brand-100 disabled:opacity-40">
                + Adicionar
              </button>
            </div>
            <div className="mt-4 flex gap-2">
              <button onClick={() => setEtapa("tipo")} disabled={enviando}
                className="flex-1 rounded-xl border border-ink-200 py-2.5 text-sm text-ink-600 transition hover:bg-ink-50 disabled:opacity-50">
                ← Voltar
              </button>
              <button
                disabled={enviando || listaFinal().length === 0}
                onClick={() => concluir(true, true, listaFinal())}
                className="flex-1 rounded-xl border border-amber-300 bg-amber-50 py-2.5 text-sm font-semibold text-amber-900 transition hover:bg-amber-100 disabled:opacity-50">
                {enviando
                  ? "Finalizando…"
                  : `Finalizar (${listaFinal().length} exame${listaFinal().length !== 1 ? "s" : ""})`}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function CampoInfo({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div>
      <dt className="text-xs text-ink-500">{rotulo}</dt>
      <dd className="mt-0.5 text-sm font-medium text-ink-900">{valor}</dd>
    </div>
  );
}

function TriagemItem({
  triagem, relatorio, onBaixarRelatorio, onEmitirRelatorio, onVerHistoricoVacinacao,
}: {
  triagem: { id: string; data: string; motivo: string; corDeRisco: "VERMELHO" | "AMARELO" | "VERDE"; pesoTotal: number; aplicacaoVacina?: boolean };
  relatorio: RelatorioSalvo | null;
  onBaixarRelatorio: (r: RelatorioSalvo) => void;
  onEmitirRelatorio: () => void;
  onVerHistoricoVacinacao: () => void;
}) {
  const ehVacina = !!triagem.aplicacaoVacina;
  const corConfig = {
    VERMELHO: { bg: "bg-red-50",    ring: "ring-red-200",    text: "text-red-700",    ponto: "🔴", rotulo: "Vermelho" },
    AMARELO:  { bg: "bg-amber-50",  ring: "ring-amber-200",  text: "text-amber-700",  ponto: "🟡", rotulo: "Amarelo"  },
    VERDE:    { bg: "bg-green-50",  ring: "ring-green-200",  text: "text-green-700",  ponto: "🟢", rotulo: "Verde"    },
  }[triagem.corDeRisco];

  const dataFormatada = (() => {
    try {
      return new Date(triagem.data).toLocaleDateString("pt-BR");
    } catch {
      return triagem.data;
    }
  })();

  return (
    <div className="flex flex-wrap items-start justify-between gap-3 rounded-xl border border-ink-200/60 bg-ink-50/40 px-4 py-3">
      <div className="space-y-0.5 min-w-0">
        <p className="text-xs text-ink-500">
          <span className="font-medium text-ink-700">Data:</span> {dataFormatada}
        </p>
        <p className="text-xs text-ink-500">
          <span className="font-medium text-ink-700">Motivo:</span>{" "}
          {ehVacina ? "Aplicação de vacina" : triagem.motivo}
        </p>
      </div>
      <div className="flex flex-wrap items-center gap-2 shrink-0">
        {ehVacina ? (
          <>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-emerald-200">
              💉 Vacinação
            </span>
            <button
              type="button"
              onClick={onVerHistoricoVacinacao}
              className="inline-flex items-center gap-1 rounded-lg border border-emerald-300 bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 transition hover:bg-emerald-100"
              title="Ver o histórico de vacinas aplicadas deste paciente"
            >
              💉 Ver histórico de vacinação
            </button>
          </>
        ) : (
          <>
            <span
              className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ${corConfig.bg} ${corConfig.ring} ${corConfig.text}`}
            >
              {corConfig.ponto} {corConfig.rotulo}
            </span>
            <span className="text-xs text-ink-500">
              | PesoTotal: <strong className="text-ink-700">{triagem.pesoTotal}</strong>
            </span>
            {relatorio ? (
              <button
                type="button"
                onClick={() => onBaixarRelatorio(relatorio)}
                className="inline-flex items-center gap-1 rounded-lg border border-brand-300 bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 transition hover:bg-brand-100"
                title="Ver/baixar o relatório clínico já emitido para este atendimento"
              >
                📄 Ver Relatório
              </button>
            ) : (
              <button
                type="button"
                onClick={onEmitirRelatorio}
                className="inline-flex items-center gap-1 rounded-lg border border-ink-300 bg-white px-2.5 py-1 text-xs font-medium text-ink-700 transition hover:border-brand-400 hover:bg-brand-50 hover:text-brand-700"
                title="Emitir o relatório clínico deste atendimento"
              >
                ✍ Emitir Relatório
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}

function ModalHistoricoVacinacao({ vacinas, onFechar }: {
  vacinas: VacinaAplicadaDTO[];
  onFechar: () => void;
}) {
  function formatarData(iso: string): string {
    const partes = (iso ?? "").slice(0, 10).split("-");
    if (partes.length === 3) {
      const [a, m, d] = partes;
      return `${d}/${m}/${a}`;
    }
    return iso;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="w-full max-w-2xl rounded-2xl bg-white p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-lg">💉</span>
            <h3 className="text-base font-semibold text-ink-900">Histórico de Vacinas Aplicadas</h3>
          </div>
          <button onClick={onFechar} className="flex h-7 w-7 items-center justify-center rounded-full text-ink-400 hover:bg-ink-100">✕</button>
        </div>

        {vacinas.length === 0 ? (
          <p className="rounded-xl bg-ink-50 px-4 py-6 text-center text-sm text-ink-500">
            Nenhuma vacina aplicada registrada para este paciente.
          </p>
        ) : (
          <div className="max-h-[60vh] overflow-y-auto rounded-xl border border-ink-100">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-ink-100 bg-ink-50/70">
                  {["Data", "Vacina", "Médico", "Lote / Selo"].map((col) => (
                    <th key={col} className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wider text-ink-500">
                      {col}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {vacinas.map((v) => (
                  <tr key={v.doseId} className="hover:bg-ink-50/40">
                    <td className="px-4 py-2.5 text-ink-600">{formatarData(v.dataAplicacao)}</td>
                    <td className="px-4 py-2.5 font-medium text-ink-900">{v.rotulo}</td>
                    <td className="px-4 py-2.5 text-ink-800">{v.medico || "—"}</td>
                    <td className="px-4 py-2.5 text-ink-800">{v.lote || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="mt-5 flex justify-end">
          <button onClick={onFechar} className="btn-primary w-auto">Fechar</button>
        </div>
      </div>
    </div>
  );
}

function BotaoAcao({
  titulo, onClick, destaque = false, disabled = false, motivoDesabilitado,
}: {
  titulo: string;
  onClick: () => void;
  destaque?: boolean;
  disabled?: boolean;
  motivoDesabilitado?: string;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      title={disabled ? motivoDesabilitado : undefined}
      className={
        "rounded-xl border px-4 py-4 text-sm font-medium transition focus:outline-none focus:ring-4 focus:ring-brand-100 " +
        (disabled
          ? "cursor-not-allowed border-ink-200 bg-ink-100 text-ink-400"
          : destaque
            ? "border-brand-400 bg-brand-50 text-brand-700 hover:bg-brand-100"
            : "border-ink-300 bg-white text-ink-700 hover:border-brand-400 hover:bg-brand-50 hover:text-brand-700")
      }
    >
      {titulo}
    </button>
  );
}
