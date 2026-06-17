import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import {
  criarRacoesAdminService,
  ROTULOS_COMORBIDADE,
  ROTULOS_FAIXA,
  ROTULOS_PORTE,
  type Comorbidade,
  type FaixaEtaria,
  type Porte,
  type RacaoAdminDTO,
  type RequisicaoRacaoDTO,
} from "./racaoAdminService";

const FAIXAS: FaixaEtaria[] = ["FILHOTE", "ADULTO", "SENIOR"];
const PORTES: Porte[] = ["PEQUENO", "MEDIO", "GRANDE"];
const COMORBIDADES: Comorbidade[] = ["NENHUMA", "OBESIDADE", "DIABETES", "DOENCA_RENAL"];

const VAZIA: RequisicaoRacaoDTO = {
  fabricante: "",
  linha: "",
  densidadeCaloricaKcalPorKg: 3500,
  faixasIndicadas: ["ADULTO"],
  portesIndicados: ["MEDIO"],
  comorbidadesIndicadas: ["NENHUMA"],
};

/**
 * Painel administrativo do catálogo de rações (F-11). CRUD com soft-delete:
 * desativar uma ração a remove das recomendações mas preserva a integridade
 * histórica dos planos nutricionais que já a prescreveram.
 */
export function AdminCatalogoRacoes() {
  const { apiFetch } = useAuth();
  const service = useMemo(() => criarRacoesAdminService(apiFetch), [apiFetch]);

  const [racoes, setRacoes] = useState<RacaoAdminDTO[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);
  const [mostrandoApenasAtivas, setMostrandoApenasAtivas] = useState(true);

  // Modal de edição/criação
  const [editando, setEditando] = useState<RacaoAdminDTO | null>(null);
  const [criando, setCriando] = useState(false);
  const [form, setForm] = useState<RequisicaoRacaoDTO>(VAZIA);
  const [salvando, setSalvando] = useState(false);

  // Confirmação de desativação
  const [confirmandoDesativacao, setConfirmandoDesativacao] = useState<{
    racao: RacaoAdminDTO; planosAfetados: number;
  } | null>(null);

  useEffect(() => { carregar(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  function carregar() {
    service.listar()
      .then(setRacoes)
      .catch(e => { setErro(e.message); setRacoes([]); });
  }

  function abrirCriacao() {
    setForm(VAZIA);
    setEditando(null);
    setCriando(true);
    setErro(null);
  }

  function abrirEdicao(racao: RacaoAdminDTO) {
    setForm({
      fabricante: racao.fabricante,
      linha: racao.linha,
      densidadeCaloricaKcalPorKg: Number(racao.densidadeCaloricaKcalPorKg),
      faixasIndicadas: racao.faixasIndicadas,
      portesIndicados: racao.portesIndicados,
      comorbidadesIndicadas: racao.comorbidadesIndicadas.length === 0
        ? ["NENHUMA"] : racao.comorbidadesIndicadas,
    });
    setEditando(racao);
    setCriando(false);
    setErro(null);
  }

  function fecharModal() {
    setEditando(null);
    setCriando(false);
  }

  async function salvar() {
    if (!form.fabricante.trim() || !form.linha.trim()) {
      setErro("Fabricante e Linha são obrigatórios.");
      return;
    }
    if (form.densidadeCaloricaKcalPorKg <= 0) {
      setErro("Densidade calórica deve ser positiva.");
      return;
    }
    if (form.faixasIndicadas.length === 0 || form.portesIndicados.length === 0) {
      setErro("Marque ao menos uma faixa etária e um porte indicados.");
      return;
    }
    setErro(null);
    setSalvando(true);
    try {
      if (editando) {
        await service.atualizar(editando.id, form);
        setSucesso(`"${form.fabricante} ${form.linha}" atualizada com sucesso.`);
      } else {
        await service.criar(form);
        setSucesso(`"${form.fabricante} ${form.linha}" criada com sucesso.`);
      }
      fecharModal();
      carregar();
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirConfirmacaoDesativacao(racao: RacaoAdminDTO) {
    try {
      const imp = await service.impacto(racao.id);
      setConfirmandoDesativacao({ racao, planosAfetados: imp.planosAfetados });
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function confirmarDesativacao() {
    if (!confirmandoDesativacao) return;
    try {
      await service.desativar(confirmandoDesativacao.racao.id);
      setSucesso(`"${confirmandoDesativacao.racao.descricaoCurta}" desativada. O histórico de planos foi preservado.`);
      setConfirmandoDesativacao(null);
      carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function reativar(racao: RacaoAdminDTO) {
    try {
      await service.reativar(racao.id);
      setSucesso(`"${racao.descricaoCurta}" reativada.`);
      carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  const visiveis = useMemo(() => {
    if (!racoes) return [];
    return mostrandoApenasAtivas ? racoes.filter(r => !r.desativada) : racoes;
  }, [racoes, mostrandoApenasAtivas]);

  return (
    <div className="mx-auto max-w-6xl px-6 py-8 space-y-6">
      <header className="flex items-baseline justify-between">
        <div>
          <h1 className="text-2xl font-bold text-ink-900">Catálogo de Rações</h1>
          <p className="text-sm text-ink-500">
            Gerencie as rações disponíveis para prescrição dos médicos. Soft-delete preserva o histórico clínico.
          </p>
        </div>
        <button
          type="button"
          onClick={abrirCriacao}
          className="rounded-xl border border-brand-500 bg-brand-500 px-5 py-2 text-sm font-medium text-white hover:bg-brand-600"
        >
          + Nova Ração
        </button>
      </header>

      {erro && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {erro}
          <button onClick={() => setErro(null)} className="ml-3 underline">fechar</button>
        </div>
      )}
      {sucesso && (
        <div role="status" className="rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">
          {sucesso}
          <button onClick={() => setSucesso(null)} className="ml-3 underline">fechar</button>
        </div>
      )}

      <div className="card p-5 space-y-4">
        <div className="flex items-center justify-between">
          <span className="text-sm text-ink-600">
            {visiveis.length} {visiveis.length === 1 ? "ração" : "rações"}
            {!mostrandoApenasAtivas && racoes && (
              <span className="ml-2 text-xs text-ink-400">
                ({racoes.filter(r => r.desativada).length} desativadas)
              </span>
            )}
          </span>
          <label className="flex items-center gap-2 text-xs text-ink-600">
            <input
              type="checkbox"
              checked={mostrandoApenasAtivas}
              onChange={e => setMostrandoApenasAtivas(e.target.checked)}
              className="rounded"
            />
            Mostrar apenas ativas
          </label>
        </div>

        {racoes === null ? (
          <div className="h-40 animate-pulse rounded-lg bg-ink-100" />
        ) : visiveis.length === 0 ? (
          <p className="py-8 text-center text-sm text-ink-500">
            Nenhuma ração encontrada. Use "+ Nova Ração" para criar a primeira.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b-2 border-ink-200 text-left text-xs text-ink-500">
                  <th className="py-2 pr-3">Ração</th>
                  <th className="py-2 pr-3">Densidade</th>
                  <th className="py-2 pr-3">Faixas</th>
                  <th className="py-2 pr-3">Portes</th>
                  <th className="py-2 pr-3">Comorbidades</th>
                  <th className="py-2 pr-3">Status</th>
                  <th className="py-2">Ações</th>
                </tr>
              </thead>
              <tbody>
                {visiveis.map(r => (
                  <tr key={r.id} className={"border-b border-ink-100 " + (r.desativada ? "opacity-60" : "")}>
                    <td className="py-2 pr-3">
                      <div className="font-medium text-ink-900">{r.fabricante}</div>
                      <div className="text-xs text-ink-500">{r.linha}</div>
                    </td>
                    <td className="py-2 pr-3 whitespace-nowrap">{r.densidadeCaloricaKcalPorKg} kcal/kg</td>
                    <td className="py-2 pr-3 text-xs">
                      {r.faixasIndicadas.map(f => ROTULOS_FAIXA[f]).join(", ")}
                    </td>
                    <td className="py-2 pr-3 text-xs">
                      {r.portesIndicados.map(p => ROTULOS_PORTE[p].split(" ")[0]).join(", ")}
                    </td>
                    <td className="py-2 pr-3 text-xs">
                      {r.comorbidadesIndicadas.map(c => ROTULOS_COMORBIDADE[c]).join(", ")}
                    </td>
                    <td className="py-2 pr-3">
                      {r.desativada ? (
                        <span className="rounded-full bg-ink-100 px-2 py-0.5 text-xs text-ink-600">Desativada</span>
                      ) : (
                        <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs text-emerald-700">Ativa</span>
                      )}
                    </td>
                    <td className="py-2 space-x-2 whitespace-nowrap">
                      <button
                        type="button"
                        onClick={() => abrirEdicao(r)}
                        className="text-xs text-brand-700 hover:underline"
                      >
                        Editar
                      </button>
                      {r.desativada ? (
                        <button
                          type="button"
                          onClick={() => reativar(r)}
                          className="text-xs text-emerald-700 hover:underline"
                        >
                          Reativar
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => abrirConfirmacaoDesativacao(r)}
                          className="text-xs text-red-600 hover:underline"
                        >
                          Desativar
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal de criação/edição */}
      {(criando || editando) && (
        <Modal titulo={editando ? `Editar ração` : "Nova ração"} onFechar={fecharModal}>
          <div className="space-y-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <Campo label="Fabricante *">
                <input
                  type="text"
                  value={form.fabricante}
                  onChange={e => setForm({ ...form, fabricante: e.target.value })}
                  placeholder="Ex.: Royal Canin"
                  className="w-full rounded border border-ink-300 px-3 py-2 text-sm"
                />
              </Campo>
              <Campo label="Linha / Nome do produto *">
                <input
                  type="text"
                  value={form.linha}
                  onChange={e => setForm({ ...form, linha: e.target.value })}
                  placeholder="Ex.: Mini Adult"
                  className="w-full rounded border border-ink-300 px-3 py-2 text-sm"
                />
              </Campo>
            </div>

            <Campo label="Densidade calórica (kcal/kg) *">
              <input
                type="number"
                min={1}
                step={50}
                value={form.densidadeCaloricaKcalPorKg}
                onChange={e => setForm({ ...form, densidadeCaloricaKcalPorKg: Number.parseInt(e.target.value || "0", 10) })}
                className="w-full rounded border border-ink-300 px-3 py-2 text-sm"
              />
              <span className="mt-1 block text-xs text-ink-500">
                Valor típico: 3000-4200 kcal/kg para rações premium.
              </span>
            </Campo>

            <Campo label="Faixas etárias indicadas *">
              <div className="flex flex-wrap gap-2">
                {FAIXAS.map(f => (
                  <ChipCheckbox
                    key={f}
                    label={ROTULOS_FAIXA[f]}
                    ativo={form.faixasIndicadas.includes(f)}
                    onToggle={() => setForm({
                      ...form,
                      faixasIndicadas: form.faixasIndicadas.includes(f)
                        ? form.faixasIndicadas.filter(x => x !== f)
                        : [...form.faixasIndicadas, f],
                    })}
                  />
                ))}
              </div>
            </Campo>

            <Campo label="Portes indicados *">
              <div className="flex flex-wrap gap-2">
                {PORTES.map(p => (
                  <ChipCheckbox
                    key={p}
                    label={ROTULOS_PORTE[p]}
                    ativo={form.portesIndicados.includes(p)}
                    onToggle={() => setForm({
                      ...form,
                      portesIndicados: form.portesIndicados.includes(p)
                        ? form.portesIndicados.filter(x => x !== p)
                        : [...form.portesIndicados, p],
                    })}
                  />
                ))}
              </div>
            </Campo>

            <Campo label="Comorbidades que esta ração cobre">
              <div className="flex flex-wrap gap-2">
                {COMORBIDADES.map(c => (
                  <ChipCheckbox
                    key={c}
                    label={ROTULOS_COMORBIDADE[c]}
                    ativo={form.comorbidadesIndicadas.includes(c)}
                    onToggle={() => setForm({
                      ...form,
                      comorbidadesIndicadas: form.comorbidadesIndicadas.includes(c)
                        ? form.comorbidadesIndicadas.filter(x => x !== c)
                        : [...form.comorbidadesIndicadas, c],
                    })}
                  />
                ))}
              </div>
              <span className="mt-1 block text-xs text-ink-500">
                "Nenhuma" indica ração de uso geral, sem foco em comorbidade.
              </span>
            </Campo>

            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={fecharModal} disabled={salvando} className="btn-ghost text-sm">
                Cancelar
              </button>
              <button
                type="button"
                onClick={salvar}
                disabled={salvando}
                className="rounded-xl border border-brand-500 bg-brand-500 px-5 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
              >
                {salvando ? "Salvando…" : editando ? "Salvar alterações" : "Criar ração"}
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Confirmação de desativação */}
      {confirmandoDesativacao && (
        <Modal titulo="Confirmar desativação" onFechar={() => setConfirmandoDesativacao(null)}>
          <div className="space-y-3">
            <p className="text-sm text-ink-800">
              Você está prestes a desativar a ração{" "}
              <strong>{confirmandoDesativacao.racao.descricaoCurta}</strong>.
            </p>
            {confirmandoDesativacao.planosAfetados > 0 ? (
              <div className="rounded-lg border border-amber-300 bg-amber-50 p-3 text-sm text-amber-900">
                ⚠ Esta ração está vinculada a <strong>{confirmandoDesativacao.planosAfetados} plano(s)</strong> nutricional(is) já prescrito(s).
                {" "}O histórico continuará referenciando esta ração — mas ela não poderá mais ser prescrita
                em novos planos. Pode ser reativada depois se necessário.
              </div>
            ) : (
              <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">
                ✓ Esta ração ainda não foi prescrita em nenhum plano. Pode desativar com tranquilidade.
              </div>
            )}
            <div className="flex justify-end gap-3 pt-2">
              <button onClick={() => setConfirmandoDesativacao(null)} className="btn-ghost text-sm">
                Cancelar
              </button>
              <button
                onClick={confirmarDesativacao}
                className="rounded-xl border border-red-500 bg-red-500 px-5 py-2 text-sm font-medium text-white hover:bg-red-600"
              >
                Desativar ração
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}

// ── Subcomponentes ────────────────────────────────────────────────────────

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

function Campo({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="text-xs font-medium text-ink-700">{label}</span>
      <div className="mt-1">{children}</div>
    </label>
  );
}

function ChipCheckbox({ label, ativo, onToggle }: { label: string; ativo: boolean; onToggle: () => void }) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={
        "rounded-full border-2 px-3 py-1 text-xs font-medium transition " +
        (ativo
          ? "border-brand-500 bg-brand-100 text-brand-800"
          : "border-ink-300 bg-white text-ink-600 hover:bg-ink-50")
      }
    >
      {ativo && "✓ "}{label}
    </button>
  );
}
