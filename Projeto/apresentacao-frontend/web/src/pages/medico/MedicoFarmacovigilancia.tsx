import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { SignaturePad, type SignaturePadHandle } from "./SignaturePad";
import {
  HORARIOS_SUGERIDOS,
  ROTULOS_FREQUENCIA,
  ROTULOS_MANEJO,
  ROTULOS_TAG,
  ROTULOS_VIA,
  criarFarmacovigilanciaMedicoService,
  fmt,
  type ContextoPacienteDTO,
  type DetalheItemDTO,
  type Frequencia,
  type HistoricoDTO,
  type InteracaoDTO,
  type ManejoAlimentar,
  type MedicamentoDTO,
  type PrescricaoDTO,
  type RascunhoItem,
  type ResultadoValidacaoDTO,
  type TagClinica,
  type TemplateDTO,
  type ViaAdministracao,
  type ViolacaoDTO,
} from "./farmacovigilanciaService";

type ItemForm = RascunhoItem & {
  horarios: string;          // CSV "08:00,20:00" para edição
  notaCuidado: string;       // sobrescreve a nota do catálogo
};

export function MedicoFarmacovigilancia() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const navigate = useNavigate();
  const { apiFetch } = useAuth();
  const service = useMemo(() => criarFarmacovigilanciaMedicoService(apiFetch), [apiFetch]);

  // Cabeçalho/contexto do paciente
  const [contexto, setContexto] = useState<ContextoPacienteDTO | null>(null);
  const [tagsAtivas, setTagsAtivas] = useState<TagClinica[]>([]);
  const [alergiasTexto, setAlergiasTexto] = useState<string>("");

  // Catálogo, interações e templates
  const [catalogo, setCatalogo] = useState<MedicamentoDTO[]>([]);
  const [interacoes, setInteracoes] = useState<InteracaoDTO[]>([]);
  const [templates, setTemplates] = useState<TemplateDTO[]>([]);
  const [historico, setHistorico] = useState<HistoricoDTO | null>(null);
  const [vigente, setVigente] = useState<PrescricaoDTO | null>(null);

  // Itens da prescrição em edição
  const [itens, setItens] = useState<ItemForm[]>([]);
  const [instrucoesGerais, setInstrucoesGerais] = useState<string[]>([]);
  const [novaInstrucao, setNovaInstrucao] = useState("");

  // Validação ao vivo
  const [validacao, setValidacao] = useState<ResultadoValidacaoDTO | null>(null);

  // Modais e ações
  const [mostrandoTemplates, setMostrandoTemplates] = useState(false);
  const [mostrandoHistorico, setMostrandoHistorico] = useState(false);
  const [mostrandoAddMedicamento, setMostrandoAddMedicamento] = useState(false);
  const [mostrandoAssinatura, setMostrandoAssinatura] = useState(false);
  const [assinaturaVazia, setAssinaturaVazia] = useState(true);
  const padRef = useRef<SignaturePadHandle>(null);

  const [erro, setErro] = useState<string | null>(null);
  const [finalizando, setFinalizando] = useState(false);
  const [prescricaoFinalizada, setPrescricaoFinalizada] = useState<PrescricaoDTO | null>(null);

  // Catálogo helper
  const medById = useMemo(() => {
    const m = new Map<string, MedicamentoDTO>();
    catalogo.forEach(c => m.set(c.id, c));
    return m;
  }, [catalogo]);

  // ── Carregamento inicial ──────────────────────────────────────────────────
  useEffect(() => {
    if (!pacienteId) return;
    service.contexto(pacienteId)
      .then(ctx => {
        setContexto(ctx);
        setTagsAtivas(ctx.tagsClinicasDerivadas);
        setAlergiasTexto(ctx.alergiasDoPaciente.join(", "));
      })
      .catch((e: Error) => setErro(`Não foi possível resolver o paciente: ${e.message}`));
    service.catalogo().then(setCatalogo).catch(() => {});
    service.interacoes().then(setInteracoes).catch(() => {});
    service.templates().then(setTemplates).catch(() => {});
    service.historico(pacienteId).then(setHistorico).catch(() => {});
    service.buscarVigente(pacienteId).then(setVigente).catch(() => {});
  }, [service, pacienteId]);

  // ── Validação ao vivo (debounced) ─────────────────────────────────────────
  useEffect(() => {
    if (!contexto || itens.length === 0) { setValidacao(null); return; }
    const pesoNumero = typeof contexto.pesoPacienteKg === "number"
      ? contexto.pesoPacienteKg
      : Number.parseFloat(String(contexto.pesoPacienteKg));
    if (!Number.isFinite(pesoNumero) || pesoNumero <= 0) return;

    const timeout = window.setTimeout(() => {
      service.validar({
        pesoPacienteKg: pesoNumero,
        tagsClinicas: tagsAtivas,
        alergias: parseAlergias(alergiasTexto),
        itens: itens.map(toRascunho),
      }).then(setValidacao).catch(() => setValidacao(null));
    }, 350);
    return () => window.clearTimeout(timeout);
  }, [itens, tagsAtivas, alergiasTexto, contexto, service]);

  // ── Operações do form ─────────────────────────────────────────────────────

  function adicionarMedicamento(med: MedicamentoDTO) {
    const viaPadrao = med.viasPermitidas[0];
    const freq: Frequencia = "UMA_VEZ_DIA";
    setItens(prev => [...prev, {
      medicamentoId: med.id,
      doseMgPorKg: Number(med.doseMaximaMgPorKg) / 2 || 0.5,
      duracaoDias: 7,
      frequencia: freq,
      via: viaPadrao,
      horarios: HORARIOS_SUGERIDOS[freq].join(","),
      notaCuidado: med.notaCuidado ?? "",
    }]);
    setMostrandoAddMedicamento(false);
  }

  function removerItem(indice: number) {
    setItens(prev => prev.filter((_, i) => i !== indice));
  }

  function atualizarItem(indice: number, mudancas: Partial<ItemForm>) {
    setItens(prev => prev.map((it, i) => {
      if (i !== indice) return it;
      const novo = { ...it, ...mudancas };
      // Quando muda a frequência, atualiza os horários sugeridos
      if (mudancas.frequencia && mudancas.frequencia !== it.frequencia) {
        novo.horarios = HORARIOS_SUGERIDOS[mudancas.frequencia].join(",");
      }
      return novo;
    }));
  }

  function aplicarTemplate(t: TemplateDTO) {
    const novos: ItemForm[] = t.itens.map(it => {
      const med = medById.get(it.medicamentoId);
      return {
        medicamentoId: it.medicamentoId,
        doseMgPorKg: Number(it.doseMgPorKg),
        duracaoDias: it.duracaoDias,
        frequencia: it.frequencia,
        via: it.via,
        horarios: HORARIOS_SUGERIDOS[it.frequencia].join(","),
        notaCuidado: med?.notaCuidado ?? "",
      };
    });
    setItens(novos);
    setMostrandoTemplates(false);
  }

  function toggleTag(tag: TagClinica) {
    setTagsAtivas(prev =>
      prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag]);
  }

  function adicionarInstrucao() {
    const t = novaInstrucao.trim();
    if (!t) return;
    setInstrucoesGerais(prev => [...prev, t]);
    setNovaInstrucao("");
  }

  function removerInstrucao(i: number) {
    setInstrucoesGerais(prev => prev.filter((_, k) => k !== i));
  }

  // ── Finalização ──────────────────────────────────────────────────────────

  function abrirAssinatura() {
    if (!contexto) {
      setErro("Paciente ainda carregando — aguarde.");
      return;
    }
    if (itens.length === 0) {
      setErro("Adicione ao menos um medicamento antes de finalizar.");
      return;
    }
    if (validacao && !validacao.podeFinalizar) {
      setErro("Existem violações de segurança que impedem a finalização. Corrija antes de assinar.");
      return;
    }
    setErro(null);
    setAssinaturaVazia(true);
    setMostrandoAssinatura(true);
  }

  async function confirmarFinalizacao() {
    if (!contexto) return;
    const imagem = padRef.current?.toDataURL();
    if (!imagem) { setErro("Assinatura é obrigatória."); return; }
    setErro(null);
    setFinalizando(true);
    try {
      const pesoNumero = Number(contexto.pesoPacienteKg);
      const finalizada = await service.finalizarDireto({
        pacienteId: contexto.pacienteId,
        tutorId: contexto.tutorId,
        pesoPacienteKg: pesoNumero,
        tagsClinicas: tagsAtivas,
        alergias: parseAlergias(alergiasTexto),
        itens: itens.map(it => ({
          ...toRascunho(it),
          horarios: parseHorarios(it.horarios),
          notaCuidado: it.notaCuidado.trim() || null,
        })),
        instrucoesGerais,
        imagemAssinaturaBase64: imagem,
      });
      setPrescricaoFinalizada(finalizada);
      setMostrandoAssinatura(false);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setFinalizando(false);
    }
  }

  // ── Render ────────────────────────────────────────────────────────────────

  if (prescricaoFinalizada && contexto) {
    return (
      <PdfPrescricao
        prescricao={prescricaoFinalizada}
        nomePet={contexto.nomePet}
        nomeTutor={contexto.nomeTutor}
        onVoltar={() => navigate(`/medico/prontuario/${pacienteId}`)}
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="flex items-center justify-between">
        <button onClick={() => navigate(`/medico/prontuario/${pacienteId}`)} className="btn-ghost text-sm">
          ← Voltar ao Prontuário
        </button>
        <h1 className="text-xl font-bold text-ink-900">
          Central de Farmacovigilância e Prescrição — {contexto?.nomePet ?? "…"}
        </h1>
        <div className="w-32" />
      </div>

      {erro && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {erro}
        </div>
      )}

      {vigente && (
        <div role="alert" className="rounded-xl border-2 border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
          <p className="font-semibold">
            ⚠ Este paciente já tem uma prescrição vigente emitida em{" "}
            {new Date(vigente.assinatura.assinadoEm).toLocaleDateString("pt-BR")}.
          </p>
          <p className="mt-1">
            Ao finalizar uma nova prescrição, a anterior será marcada como SUBSTITUÍDA
            (permanece no histórico para auditoria) e a nova passa a valer para o tutor.
          </p>
        </div>
      )}

      {/* Ações superiores: Templates / Histórico */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <BotaoAcao
          titulo="📋 Aplicar Template"
          subtitulo={`${templates.length} pré-configurados (Gastroproteção, Antiemético...)`}
          onClick={() => setMostrandoTemplates(true)}
        />
        <BotaoAcao
          titulo="📚 Histórico de Prescrições"
          subtitulo={`${historico?.prescricoes.length ?? 0} prescrições anteriores`}
          onClick={() => setMostrandoHistorico(true)}
        />
      </div>

      {/* Alergias e Tags */}
      <div className="card p-5 space-y-3 border-2 border-red-200 bg-red-50/40">
        <h2 className="text-base font-semibold text-red-800">⚠ Alergias e Tags Clínicas</h2>
        <div>
          <label className="text-xs text-ink-500">Alergias do paciente (separe por vírgula)</label>
          <input
            type="text"
            value={alergiasTexto}
            onChange={e => setAlergiasTexto(e.target.value)}
            placeholder="Ex.: penicilina, cefalosporinas"
            className="mt-1 w-full rounded-lg border border-red-300 bg-white px-3 py-2 text-sm"
          />
          <p className="mt-1 text-xs text-ink-500">
            O sistema irá bloquear automaticamente medicamentos que contenham qualquer componente listado.
          </p>
        </div>
        <div>
          <p className="mb-2 text-xs text-ink-500">Tags clínicas ativas (derivadas + médico marca extras)</p>
          <div className="flex flex-wrap gap-2">
            {(["GERIATRICO", "INSUFICIENCIA_RENAL", "INSUFICIENCIA_HEPATICA", "CARDIOPATA"] as TagClinica[]).map(tag => {
              const ativa = tagsAtivas.includes(tag);
              return (
                <button
                  key={tag}
                  type="button"
                  onClick={() => toggleTag(tag)}
                  className={
                    "rounded-full border px-3 py-1 text-xs font-medium transition " +
                    (ativa
                      ? "border-amber-500 bg-amber-100 text-amber-800"
                      : "border-ink-300 bg-white text-ink-600 hover:bg-ink-50")
                  }
                >
                  {ativa && "✓ "}{ROTULOS_TAG[tag]}
                </button>
              );
            })}
          </div>
          {tagsAtivas.some(t => t !== "CARDIOPATA") && (
            <p className="mt-2 text-xs text-amber-700">
              ⚠ Redutor automático de 25% será aplicado no teto de dosagem máxima.
            </p>
          )}
        </div>
      </div>

      {/* Lista de medicamentos */}
      <div className="card p-5 space-y-3">
        <div className="flex items-baseline justify-between">
          <div>
            <h2 className="text-base font-semibold text-ink-900">Lista de Medicamentos</h2>
            <p className="text-xs text-ink-500">
              Peso do paciente: <strong>{fmt(contexto?.pesoPacienteKg ?? 0, 2)} kg</strong>
            </p>
          </div>
          <button
            type="button"
            onClick={() => setMostrandoAddMedicamento(true)}
            className="rounded-lg border border-brand-400 bg-brand-50 px-4 py-1.5 text-sm font-medium text-brand-700 hover:bg-brand-100"
          >
            + Adicionar Medicamento
          </button>
        </div>

        {itens.length === 0 && (
          <p className="text-sm text-ink-500">
            Nenhum medicamento adicionado ainda. Use o botão acima ou aplique um template.
          </p>
        )}

        <div className="space-y-3">
          {itens.map((item, i) => {
            const med = medById.get(item.medicamentoId);
            const detalhe = validacao?.detalhes.find(d => d.medicamentoId === item.medicamentoId);
            return (
              <ItemEditavel
                key={i}
                indice={i}
                item={item}
                medicamento={med}
                detalhe={detalhe}
                onChange={mudancas => atualizarItem(i, mudancas)}
                onRemover={() => removerItem(i)}
              />
            );
          })}
        </div>
      </div>

      {/* Validação consolidada (violações) */}
      {validacao && validacao.violacoes.length > 0 && (
        <BannerViolacoes violacoes={validacao.violacoes} />
      )}

      {/* Instruções gerais */}
      <div className="card p-5 space-y-3">
        <h2 className="text-base font-semibold text-ink-900">Instruções de Administração para o Tutor</h2>
        <ul className="space-y-1">
          {instrucoesGerais.map((ins, i) => (
            <li key={i} className="flex items-start gap-2 rounded border border-ink-200 bg-ink-50/40 px-3 py-2 text-sm">
              <span className="flex-1">• {ins}</span>
              <button type="button" onClick={() => removerInstrucao(i)} className="text-xs text-red-600 hover:underline">
                remover
              </button>
            </li>
          ))}
          {instrucoesGerais.length === 0 && (
            <li className="text-sm text-ink-500">Nenhuma instrução adicionada ainda.</li>
          )}
        </ul>
        <div className="flex gap-2">
          <input
            type="text"
            value={novaInstrucao}
            onChange={e => setNovaInstrucao(e.target.value)}
            placeholder="Ex.: Administrar com alimento para melhor absorção"
            className="flex-1 rounded-lg border border-ink-300 px-3 py-2 text-sm"
            onKeyDown={e => { if (e.key === "Enter") { e.preventDefault(); adicionarInstrucao(); } }}
          />
          <button
            type="button"
            onClick={adicionarInstrucao}
            className="rounded-lg border border-brand-400 bg-brand-50 px-4 py-2 text-sm font-medium text-brand-700 hover:bg-brand-100"
          >
            Adicionar
          </button>
        </div>
      </div>

      {/* Resumo do Tratamento */}
      <ResumoTratamento itens={itens} />

      {/* Ação final */}
      <div className="space-y-2">
        <div className="flex justify-end">
          <button
            type="button"
            onClick={abrirAssinatura}
            disabled={!contexto || itens.length === 0 || (validacao !== null && !validacao.podeFinalizar)}
            className="rounded-xl border border-brand-500 bg-brand-500 px-6 py-3 text-base font-semibold text-white hover:bg-brand-600 disabled:opacity-50"
          >
            ✍ {vigente ? "Substituir Prescrição e Assinar" : "Finalizar e Assinar"}
          </button>
        </div>
        <p className="text-right text-xs text-ink-500">
          A prescrição só é salva no banco após a assinatura digital.
          Itens com violações de BLOQUEIO precisam ser corrigidos antes.
        </p>
      </div>

      {/* Modal: catálogo de medicamentos pra adicionar */}
      {mostrandoAddMedicamento && (
        <Modal onFechar={() => setMostrandoAddMedicamento(false)} titulo="Adicionar Medicamento ao Catálogo">
          <div className="max-h-96 space-y-2 overflow-y-auto">
            {catalogo.map(med => (
              <button
                key={med.id}
                type="button"
                onClick={() => adicionarMedicamento(med)}
                disabled={itens.some(it => it.medicamentoId === med.id)}
                className="flex w-full items-start gap-3 rounded-lg border border-ink-200 p-3 text-left text-sm hover:border-brand-400 hover:bg-brand-50/30 disabled:opacity-40"
              >
                <span className="flex-1">
                  <strong>{med.nome}</strong>
                  <br />
                  <span className="text-xs text-ink-500">
                    Dose máx: {fmt(med.doseMaximaMgPorKg, 2)} mg/kg · {ROTULOS_MANEJO[med.manejoAlimentar]}
                  </span>
                </span>
                {itens.some(it => it.medicamentoId === med.id) && (
                  <span className="text-xs text-ink-400">já adicionado</span>
                )}
              </button>
            ))}
          </div>
        </Modal>
      )}

      {/* Modal: templates */}
      {mostrandoTemplates && (
        <Modal onFechar={() => setMostrandoTemplates(false)} titulo="Templates de Prescrição">
          <p className="mb-3 text-sm text-amber-700">
            ⚠ Ao aplicar um template, os medicamentos atuais serão substituídos.
          </p>
          <div className="space-y-3">
            {templates.map(t => (
              <div key={t.id} className="rounded-lg border border-ink-200 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-semibold text-ink-900">{t.nome}</h3>
                    <p className="text-xs text-ink-500">{t.descricao}</p>
                  </div>
                  <button
                    type="button"
                    onClick={() => aplicarTemplate(t)}
                    className="rounded-lg border border-brand-500 bg-brand-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-brand-600"
                  >
                    Aplicar Template
                  </button>
                </div>
                <ul className="mt-3 space-y-1">
                  {t.itens.map((it, i) => {
                    const med = medById.get(it.medicamentoId);
                    return (
                      <li key={i} className="text-xs text-ink-700">
                        • <strong>{med?.nome ?? "?"}</strong> — {fmt(it.doseMgPorKg, 2)} mg/kg,
                        {" "}{ROTULOS_FREQUENCIA[it.frequencia]}, {it.duracaoDias} dias ({ROTULOS_VIA[it.via]})
                      </li>
                    );
                  })}
                </ul>
              </div>
            ))}
          </div>
        </Modal>
      )}

      {/* Modal: histórico */}
      {mostrandoHistorico && (
        <Modal onFechar={() => setMostrandoHistorico(false)} titulo="Histórico de Prescrições Anteriores">
          {historico === null || historico.prescricoes.length === 0 ? (
            <p className="text-sm text-ink-500">Nenhuma prescrição anterior registrada.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-ink-200 text-left text-xs text-ink-500">
                  <th className="py-2 pr-3">Data</th>
                  <th className="py-2 pr-3">Status</th>
                  <th className="py-2 pr-3">Medicamentos</th>
                  <th className="py-2">Duração</th>
                </tr>
              </thead>
              <tbody>
                {historico.prescricoes.map(p => (
                  <tr key={p.id} className="border-b border-ink-100">
                    <td className="py-1.5 pr-3">{new Date(p.assinatura.assinadoEm).toLocaleDateString("pt-BR")}</td>
                    <td className="py-1.5 pr-3">
                      <span className={
                        "rounded-full px-2 py-0.5 text-xs " +
                        (p.status === "FINALIZADA" ? "bg-emerald-100 text-emerald-700"
                                                    : "bg-ink-100 text-ink-600")
                      }>
                        {p.status === "FINALIZADA" ? "Vigente" : "Substituída"}
                      </span>
                    </td>
                    <td className="py-1.5 pr-3">{p.itens.map(it => it.nomeMedicamento).join(", ")}</td>
                    <td className="py-1.5">
                      {Math.max(...p.itens.map(it => it.duracaoDias))} dias
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Modal>
      )}

      {/* Modal: assinatura */}
      {mostrandoAssinatura && (
        <Modal onFechar={() => setMostrandoAssinatura(false)} titulo="Assinatura Digital">
          <p className="mb-3 text-sm text-ink-600">
            Sua assinatura será anexada à prescrição, que se tornará imutável.
          </p>
          <SignaturePad ref={padRef} onChange={setAssinaturaVazia} />
          <div className="mt-5 flex justify-end gap-3">
            <button
              type="button"
              onClick={() => setMostrandoAssinatura(false)}
              disabled={finalizando}
              className="btn-ghost text-sm"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={confirmarFinalizacao}
              disabled={assinaturaVazia || finalizando}
              className="rounded-xl border border-brand-500 bg-brand-500 px-5 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
            >
              {finalizando ? "Finalizando…" : "Confirmar e Finalizar"}
            </button>
          </div>
        </Modal>
      )}

      {/* Diagnóstico de interações conhecidas — bloco discreto pro médico ver */}
      {interacoes.length > 0 && itens.length === 0 && (
        <details className="rounded-lg border border-ink-200 p-3 text-sm">
          <summary className="cursor-pointer text-ink-600">Matriz de interações conhecida ({interacoes.length} pares)</summary>
          <ul className="mt-2 space-y-1 text-xs text-ink-600">
            {interacoes.map((i, k) => {
              const a = medById.get(i.medicamentoAId)?.nome ?? "?";
              const b = medById.get(i.medicamentoBId)?.nome ?? "?";
              return <li key={k}>• {a} + {b}: <strong>{i.gravidade}</strong> — {i.descricao}</li>;
            })}
          </ul>
        </details>
      )}
    </div>
  );
}

// ── Subcomponentes ────────────────────────────────────────────────────────

function BotaoAcao({ titulo, subtitulo, onClick }: { titulo: string; subtitulo: string; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="rounded-xl border-2 border-ink-200 bg-white p-4 text-left transition hover:border-brand-400 hover:bg-brand-50/30"
    >
      <p className="text-sm font-semibold text-ink-900">{titulo}</p>
      <p className="mt-0.5 text-xs text-ink-500">{subtitulo}</p>
    </button>
  );
}

function Modal({ titulo, children, onFechar }: { titulo: string; children: React.ReactNode; onFechar: () => void }) {
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 px-4">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-base font-semibold text-ink-900">{titulo}</h3>
          <button onClick={onFechar} className="text-ink-500 hover:text-ink-800">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

function ItemEditavel({
  indice, item, medicamento, detalhe, onChange, onRemover,
}: {
  indice: number; item: ItemForm; medicamento: MedicamentoDTO | undefined;
  detalhe: DetalheItemDTO | undefined;
  onChange: (mudancas: Partial<ItemForm>) => void;
  onRemover: () => void;
}) {
  if (!medicamento) return null;

  const doseExcedeMax = detalhe && Number(item.doseMgPorKg) > Number(detalhe.doseMaximaSeguraCalculada);
  const ehAlergico = detalhe?.alergiaAplicada ?? false;
  const borderCor = ehAlergico ? "border-red-400" : doseExcedeMax ? "border-amber-400" : "border-ink-200";

  return (
    <div className={`rounded-xl border-2 ${borderCor} p-4 space-y-3`}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1">
          <p className="font-semibold text-ink-900">
            #{indice + 1} {medicamento.nome}
            {ehAlergico && <span className="ml-2 rounded bg-red-100 px-2 py-0.5 text-xs text-red-700">ALERGIA — BLOQUEIO</span>}
            {detalhe?.tagAplicada && <span className="ml-2 rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-700">redutor -25%</span>}
          </p>
          <p className="text-xs text-ink-500">{ROTULOS_MANEJO[medicamento.manejoAlimentar as ManejoAlimentar]}</p>
        </div>
        <button type="button" onClick={onRemover} className="text-xs text-red-600 hover:underline">
          Remover
        </button>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Campo label="Dose (mg/kg)">
          <input
            type="number" min={0} step={0.01}
            value={item.doseMgPorKg}
            onChange={e => onChange({ doseMgPorKg: Number.parseFloat(e.target.value || "0") })}
            className="w-full rounded border border-ink-300 px-2 py-1 text-sm"
          />
        </Campo>
        <Campo label="Duração (dias)">
          <input
            type="number" min={1}
            value={item.duracaoDias}
            onChange={e => onChange({ duracaoDias: Number.parseInt(e.target.value || "1", 10) })}
            className="w-full rounded border border-ink-300 px-2 py-1 text-sm"
          />
        </Campo>
        <Campo label="Frequência">
          <select
            value={item.frequencia}
            onChange={e => onChange({ frequencia: e.target.value as Frequencia })}
            className="w-full rounded border border-ink-300 px-2 py-1 text-sm"
          >
            {(Object.keys(ROTULOS_FREQUENCIA) as Frequencia[]).map(f => (
              <option key={f} value={f}>{ROTULOS_FREQUENCIA[f]}</option>
            ))}
          </select>
        </Campo>
        <Campo label="Via">
          <select
            value={item.via}
            onChange={e => onChange({ via: e.target.value as ViaAdministracao })}
            className="w-full rounded border border-ink-300 px-2 py-1 text-sm"
          >
            {medicamento.viasPermitidas.map(v => (
              <option key={v} value={v}>{ROTULOS_VIA[v as ViaAdministracao]}</option>
            ))}
          </select>
        </Campo>
      </div>

      <Campo label="Horários (separe por vírgula)">
        <input
          type="text"
          value={item.horarios}
          onChange={e => onChange({ horarios: e.target.value })}
          placeholder="08:00, 20:00"
          className="w-full rounded border border-ink-300 px-2 py-1 text-sm"
        />
      </Campo>
      <Campo label="Nota de cuidado">
        <input
          type="text"
          value={item.notaCuidado}
          onChange={e => onChange({ notaCuidado: e.target.value })}
          className="w-full rounded border border-ink-300 px-2 py-1 text-sm"
        />
      </Campo>

      {/* Detalhe da validação */}
      {detalhe && (
        <div className="rounded-lg bg-ink-50 p-3 text-xs text-ink-700">
          <p>
            <strong>Dose máx. segura:</strong> {fmt(detalhe.doseMaximaSeguraCalculada, 3)} mg/kg
            {" · "}<strong>Dose total proposta:</strong> {fmt(detalhe.doseTotalPropostaMg, 2)} mg
            {" · "}<strong>Volume:</strong> {fmt(detalhe.volumeFinalMl, 2)} ml
          </p>
        </div>
      )}
    </div>
  );
}

function Campo({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="text-xs text-ink-500">{label}</span>
      <div className="mt-1">{children}</div>
    </label>
  );
}

function BannerViolacoes({ violacoes }: { violacoes: ViolacaoDTO[] }) {
  const bloqueios = violacoes.filter(v => v.nivel === "BLOQUEIO");
  const alertas = violacoes.filter(v => v.nivel === "ALERTA");
  return (
    <div className="space-y-3">
      {bloqueios.length > 0 && (
        <div role="alert" className="rounded-xl border-2 border-red-300 bg-red-50 p-4">
          <h3 className="font-semibold text-red-800">⛔ {bloqueios.length} Bloqueio(s) de Segurança</h3>
          <ul className="mt-2 space-y-1 text-sm text-red-700">
            {bloqueios.map((v, i) => <li key={i}>• {v.mensagem}</li>)}
          </ul>
        </div>
      )}
      {alertas.length > 0 && (
        <div role="alert" className="rounded-xl border-2 border-amber-300 bg-amber-50 p-4">
          <h3 className="font-semibold text-amber-800">⚠ {alertas.length} Alerta(s)</h3>
          <ul className="mt-2 space-y-1 text-sm text-amber-800">
            {alertas.map((v, i) => <li key={i}>• {v.mensagem}</li>)}
          </ul>
        </div>
      )}
    </div>
  );
}

function ResumoTratamento({ itens }: { itens: ItemForm[] }) {
  if (itens.length === 0) return null;
  const maiorDuracao = Math.max(...itens.map(i => i.duracaoDias));
  const dataFim = new Date(); dataFim.setDate(dataFim.getDate() + maiorDuracao);
  return (
    <div className="card p-5 space-y-1">
      <h2 className="text-base font-semibold text-ink-900">Resumo do Tratamento</h2>
      <p className="text-sm">
        <strong>Data de início:</strong> {new Date().toLocaleDateString("pt-BR")}
      </p>
      <p className="text-sm">
        <strong>Data de fim:</strong> {dataFim.toLocaleDateString("pt-BR")}{" "}
        <span className="text-xs text-ink-500">(calculada pelo medicamento de ciclo mais longo — {maiorDuracao} dias)</span>
      </p>
    </div>
  );
}

// ── Vista de PDF ──────────────────────────────────────────────────────────

function PdfPrescricao({
  prescricao, nomePet, nomeTutor, onVoltar,
}: {
  prescricao: PrescricaoDTO; nomePet: string; nomeTutor: string; onVoltar: () => void;
}) {
  const a = prescricao.assinatura;
  const dataAss = new Date(a.assinadoEm).toLocaleString("pt-BR");
  return (
    <div className="space-y-4">
      <div className="print:hidden rounded-2xl border-2 border-emerald-300 bg-emerald-50 p-5">
        <div className="flex items-start gap-3">
          <span className="text-3xl">✓</span>
          <div className="flex-1">
            <h2 className="text-lg font-bold text-emerald-800">Prescrição Gerada com Sucesso</h2>
            <p className="mt-1 text-sm text-emerald-700">
              Documento imutável assinado digitalmente em {dataAss}. Visível para o tutor na aba Medicamentos.
            </p>
          </div>
        </div>
        <div className="mt-4 flex flex-wrap items-center justify-end gap-3">
          <button onClick={onVoltar} className="btn-ghost text-sm">← Voltar ao Prontuário</button>
          <button
            type="button"
            onClick={() => window.print()}
            className="rounded-xl border border-brand-500 bg-brand-500 px-6 py-3 text-base font-semibold text-white hover:bg-brand-600"
          >
            📄 Baixar Prescrição PDF
          </button>
        </div>
      </div>

      <div className="card p-8 space-y-6 print:shadow-none print:border-0">
        <header className="border-b border-ink-200 pb-4">
          <h1 className="text-2xl font-bold text-ink-900">Prescrição Farmacológica</h1>
          <dl className="mt-4 grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <div><dt className="text-xs text-ink-500">Paciente</dt><dd className="font-medium">{nomePet}</dd></div>
            <div><dt className="text-xs text-ink-500">Tutor</dt><dd className="font-medium">{nomeTutor}</dd></div>
            <div><dt className="text-xs text-ink-500">Peso considerado</dt><dd className="font-medium">{fmt(prescricao.pesoPacienteKg, 2)} kg</dd></div>
            <div><dt className="text-xs text-ink-500">Período</dt><dd className="font-medium">{prescricao.dataInicio} → {prescricao.dataFim}</dd></div>
          </dl>
        </header>

        <section>
          <h2 className="mb-3 text-base font-semibold text-ink-900">Medicamentos</h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-ink-200 text-left text-xs text-ink-500">
                <th className="py-2 pr-3">Medicamento</th>
                <th className="py-2 pr-3">Dose</th>
                <th className="py-2 pr-3">Volume</th>
                <th className="py-2 pr-3">Via</th>
                <th className="py-2 pr-3">Frequência</th>
                <th className="py-2 pr-3">Duração</th>
                <th className="py-2">Horários</th>
              </tr>
            </thead>
            <tbody>
              {prescricao.itens.map((it, i) => (
                <tr key={i} className="border-b border-ink-100 align-top">
                  <td className="py-1.5 pr-3 font-medium">{it.nomeMedicamento}</td>
                  <td className="py-1.5 pr-3">{fmt(it.doseTotalMg, 2)} mg</td>
                  <td className="py-1.5 pr-3">{fmt(it.volumeFinalMl, 2)} ml</td>
                  <td className="py-1.5 pr-3">{ROTULOS_VIA[it.via as ViaAdministracao]}</td>
                  <td className="py-1.5 pr-3">{ROTULOS_FREQUENCIA[it.frequencia as Frequencia]}</td>
                  <td className="py-1.5 pr-3">{it.duracaoDias} dias</td>
                  <td className="py-1.5">{it.horarios.join(", ")}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {prescricao.itens.some(it => it.notaCuidado) && (
            <ul className="mt-3 space-y-1 text-xs text-ink-600">
              {prescricao.itens.filter(it => it.notaCuidado).map((it, i) => (
                <li key={i}>• <strong>{it.nomeMedicamento}:</strong> {it.notaCuidado}</li>
              ))}
            </ul>
          )}
        </section>

        {prescricao.instrucoesGerais.length > 0 && (
          <section>
            <h2 className="mb-2 text-base font-semibold text-ink-900">Instruções de Administração para o Tutor</h2>
            <ul className="list-disc space-y-1 pl-5 text-sm">
              {prescricao.instrucoesGerais.map((i, k) => <li key={k}>{i}</li>)}
            </ul>
          </section>
        )}

        <section className="border-t border-ink-200 pt-4">
          <h2 className="mb-2 text-base font-semibold text-ink-900">Assinatura Digital</h2>
          <img src={a.imagemBase64} alt="Assinatura" className="mb-2 h-28 w-auto rounded border border-ink-200 bg-white p-2" />
          <p className="text-xs text-ink-500">✓ Documento assinado digitalmente em {dataAss}.</p>
        </section>
      </div>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────

function toRascunho(it: ItemForm): RascunhoItem {
  return {
    medicamentoId: it.medicamentoId,
    doseMgPorKg: Number(it.doseMgPorKg),
    duracaoDias: it.duracaoDias,
    frequencia: it.frequencia,
    via: it.via,
  };
}

function parseAlergias(texto: string): string[] {
  return texto.split(",").map(s => s.trim()).filter(s => s.length > 0);
}

function parseHorarios(texto: string): string[] {
  return texto.split(",").map(s => s.trim()).filter(s => s.length > 0);
}
