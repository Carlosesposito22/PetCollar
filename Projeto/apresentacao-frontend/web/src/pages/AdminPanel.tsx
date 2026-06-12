import { useCallback, useEffect, useState } from "react";
import { useAuth, type Perfil } from "../auth/AuthContext";

type Status = "PENDENTE" | "ATIVA" | "INADIMPLENTE" | "SUSPENSA";

type Usuario = {
  identificador: string;
  nome: string;
  perfil: Perfil;
  status: Status;
  email?: string | null;
  telefone?: string | null;
};

type PeriodoRenovacao = "MENSAL" | "TRIMESTRAL" | "ANUAL";

type Beneficio = {
  nome: string;
  periodoRenovacao: PeriodoRenovacao;
  limiteUsosPorPeriodo: number;
  carenciaDias: number;
};

type Plano = {
  id: string;
  nome: string;
  valorMensalidade: number;
  beneficios: Beneficio[];
};

// Benefícios padrão de todo plano (a princípio: Consulta e Vacinação).
const BENEFICIOS_PADRAO: Beneficio[] = [
  { nome: "Consulta", periodoRenovacao: "TRIMESTRAL", limiteUsosPorPeriodo: 4, carenciaDias: 30 },
  { nome: "Vacinação", periodoRenovacao: "ANUAL", limiteUsosPorPeriodo: 1, carenciaDias: 90 },
];

const PERIODO_OPCOES: { valor: PeriodoRenovacao; label: string }[] = [
  { valor: "MENSAL", label: "Mensal" },
  { valor: "TRIMESTRAL", label: "Trimestral" },
  { valor: "ANUAL", label: "Anual" },
];

type Aba = "funcionarios" | "tutores" | "planos";

export function AdminPanel() {
  const { apiFetch } = useAuth();
  const [aba, setAba] = useState<Aba>("funcionarios");
  const [funcionarios, setFuncionarios] = useState<Usuario[]>([]);
  const [tutores, setTutores] = useState<Usuario[]>([]);
  const [planos, setPlanos] = useState<Plano[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [criandoAberto, setCriandoAberto] = useState(false);
  const [planoModalAberto, setPlanoModalAberto] = useState(false);
  const [planoEditando, setPlanoEditando] = useState<Plano | null>(null);

  const recarregar = useCallback(async () => {
    setErro(null);
    try {
      const [f, t, p] = await Promise.all([
        apiFetch("/api/admin/funcionarios").then(asJson<Usuario[]>),
        apiFetch("/api/admin/tutores").then(asJson<Usuario[]>),
        apiFetch("/api/admin/planos").then(asJson<Plano[]>),
      ]);
      setFuncionarios(f);
      setTutores(t);
      setPlanos(p);
    } catch (err) {
      setErro((err as Error).message);
    }
  }, [apiFetch]);

  useEffect(() => { void recarregar(); }, [recarregar]);

  async function executar(label: string, url: string) {
    setErro(null);
    setAviso(null);
    try {
      const res = await apiFetch(url, { method: "POST" });
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha (HTTP ${res.status}).`);
      }
      setAviso(label);
      await recarregar();
    } catch (err) {
      setErro((err as Error).message);
    }
  }

  return (
    <>
    <main className="mx-auto max-w-6xl px-6 py-10">
        <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold text-ink-900">Gestão da clínica</h1>
            <p className="text-sm text-ink-500">
              Cadastre funcionários, gerencie tutores e configure os planos de saúde.
            </p>
          </div>
          {aba === "funcionarios" && (
            <button onClick={() => setCriandoAberto(true)} className="btn-primary w-auto">
              + Cadastrar funcionário
            </button>
          )}
          {aba === "planos" && (
            <button onClick={() => { setPlanoEditando(null); setPlanoModalAberto(true); }} className="btn-primary w-auto">
              + Novo plano
            </button>
          )}
        </div>

        <div className="mb-4 inline-flex rounded-xl bg-white p-1 ring-1 ring-ink-300/60 shadow-sm">
          <TabBtn ativo={aba === "funcionarios"} onClick={() => setAba("funcionarios")}>
            Funcionários ({funcionarios.length})
          </TabBtn>
          <TabBtn ativo={aba === "tutores"} onClick={() => setAba("tutores")}>
            Tutores ({tutores.length})
          </TabBtn>
          <TabBtn ativo={aba === "planos"} onClick={() => setAba("planos")}>
            Planos ({planos.length})
          </TabBtn>
        </div>

        {erro && (
          <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}
        {aviso && (
          <div role="status" className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-900">
            {aviso}
          </div>
        )}

        {aba === "funcionarios" ? (
          <TabelaFuncionarios
            dados={funcionarios}
            onSuspender={u => executar(
              `${u.nome} suspenso`,
              `/api/admin/funcionarios/${encodeURIComponent(u.identificador)}/suspender`,
            )}
            onReativar={u => executar(
              `${u.nome} reativado`,
              `/api/admin/funcionarios/${encodeURIComponent(u.identificador)}/reativar`,
            )}
          />
        ) : aba === "tutores" ? (
          <TabelaTutores
            dados={tutores}
            onSuspender={t => executar(
              `${t.nome} suspenso`,
              `/api/admin/tutores/${encodeURIComponent(t.identificador)}/suspender`,
            )}
            onReativar={t => executar(
              `${t.nome} reativado`,
              `/api/admin/tutores/${encodeURIComponent(t.identificador)}/reativar`,
            )}
            onConfirmarPagamento={t => executar(
              `Pagamento de ${t.nome} confirmado`,
              `/api/admin/tutores/${encodeURIComponent(t.identificador)}/confirmar-pagamento`,
            )}
          />
        ) : (
          <TabelaPlanos
            dados={planos}
            onEditar={p => { setPlanoEditando(p); setPlanoModalAberto(true); }}
            onExcluir={async p => {
              setErro(null);
              setAviso(null);
              try {
                const res = await apiFetch(`/api/admin/planos/${p.id}`, { method: "DELETE" });
                if (!res.ok) {
                  const body = await res.json().catch(() => ({} as { mensagem?: string }));
                  throw new Error(body?.mensagem ?? `Falha ao excluir (HTTP ${res.status}).`);
                }
                setAviso(`Plano "${p.nome}" excluído`);
                await recarregar();
              } catch (err) {
                setErro((err as Error).message);
              }
            }}
          />
        )}
    </main>

    {criandoAberto && (
      <CriarFuncionarioModal
        apiFetch={apiFetch}
        onFechar={() => setCriandoAberto(false)}
        onCriado={novo => {
          setAviso(`Funcionário criado · matrícula ${novo.identificador}`);
          setCriandoAberto(false);
          void recarregar();
        }}
      />
    )}

    {planoModalAberto && (
      <PlanoModal
        apiFetch={apiFetch}
        plano={planoEditando}
        onFechar={() => setPlanoModalAberto(false)}
        onSalvo={msg => {
          setAviso(msg);
          setPlanoModalAberto(false);
          void recarregar();
        }}
      />
    )}
    </>
  );
}

function TabBtn({ ativo, onClick, children }: { ativo: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      className={
        "rounded-lg px-4 py-2 text-sm font-medium transition " +
        (ativo ? "bg-brand-600 text-white shadow" : "text-ink-700 hover:bg-ink-100")
      }
    >
      {children}
    </button>
  );
}

function StatusBadge({ status }: { status: Status }) {
  const map: Record<Status, string> = {
    PENDENTE: "bg-amber-50 text-amber-700 ring-amber-100",
    ATIVA: "bg-emerald-50 text-emerald-700 ring-emerald-100",
    INADIMPLENTE: "bg-orange-50 text-orange-700 ring-orange-100",
    SUSPENSA: "bg-red-50 text-red-700 ring-red-100",
  };
  const label: Record<Status, string> = {
    PENDENTE: "Pendente",
    ATIVA: "Ativa",
    INADIMPLENTE: "Inadimplente",
    SUSPENSA: "Suspensa",
  };
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ${map[status]}`}>
      {label[status]}
    </span>
  );
}

function PerfilLabel({ perfil }: { perfil: Perfil }) {
  const map: Record<Perfil, string> = {
    TUTOR: "Tutor",
    RECEPCIONISTA: "Recepcionista",
    MEDICO_VETERINARIO: "Médico Veterinário",
    ADMIN_CLINICA: "Admin",
  };
  return <span className="text-sm text-ink-700">{map[perfil]}</span>;
}

function TabelaFuncionarios({
  dados, onSuspender, onReativar,
}: {
  dados: Usuario[];
  onSuspender: (u: Usuario) => void;
  onReativar: (u: Usuario) => void;
}) {
  if (dados.length === 0) {
    return <Vazio mensagem="Nenhum funcionário cadastrado ainda." />;
  }
  return (
    <div className="card overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-ink-100 text-xs uppercase tracking-wide text-ink-500">
          <tr>
            <Th>Matrícula</Th><Th>Nome</Th><Th>Perfil</Th><Th>Status</Th><Th className="text-right">Ações</Th>
          </tr>
        </thead>
        <tbody className="divide-y divide-ink-300/40">
          {dados.map(u => (
            <tr key={u.identificador}>
              <Td><code className="font-mono">{u.identificador}</code></Td>
              <Td>{u.nome}</Td>
              <Td><PerfilLabel perfil={u.perfil} /></Td>
              <Td><StatusBadge status={u.status} /></Td>
              <Td className="text-right">
                {u.status === "ATIVA" ? (
                  <button onClick={() => onSuspender(u)} className="btn-ghost ring-1 ring-red-200 text-red-700 hover:bg-red-50">
                    Suspender
                  </button>
                ) : (
                  <button onClick={() => onReativar(u)} className="btn-ghost ring-1 ring-emerald-200 text-emerald-700 hover:bg-emerald-50">
                    Reativar
                  </button>
                )}
              </Td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function TabelaTutores({
  dados, onSuspender, onReativar, onConfirmarPagamento,
}: {
  dados: Usuario[];
  onSuspender: (u: Usuario) => void;
  onReativar: (u: Usuario) => void;
  onConfirmarPagamento: (u: Usuario) => void;
}) {
  if (dados.length === 0) {
    return <Vazio mensagem="Nenhum tutor cadastrado ainda." />;
  }
  return (
    <div className="card overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-ink-100 text-xs uppercase tracking-wide text-ink-500">
          <tr>
            <Th>E-mail</Th><Th>Nome</Th><Th>Telefone</Th><Th>Status</Th><Th className="text-right">Ações</Th>
          </tr>
        </thead>
        <tbody className="divide-y divide-ink-300/40">
          {dados.map(u => (
            <tr key={u.identificador}>
              <Td><code className="font-mono text-xs">{u.email ?? u.identificador}</code></Td>
              <Td>{u.nome}</Td>
              <Td>{u.telefone ?? "—"}</Td>
              <Td><StatusBadge status={u.status} /></Td>
              <Td className="text-right">
                <div className="inline-flex gap-2">
                  {u.status === "PENDENTE" && (
                    <button onClick={() => onConfirmarPagamento(u)} className="btn-ghost ring-1 ring-brand-200 text-brand-700 hover:bg-brand-50">
                      Confirmar pagamento
                    </button>
                  )}
                  {u.status === "ATIVA" ? (
                    <button onClick={() => onSuspender(u)} className="btn-ghost ring-1 ring-red-200 text-red-700 hover:bg-red-50">
                      Suspender
                    </button>
                  ) : (
                    <button onClick={() => onReativar(u)} className="btn-ghost ring-1 ring-emerald-200 text-emerald-700 hover:bg-emerald-50">
                      Reativar
                    </button>
                  )}
                </div>
              </Td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Th({ children, className }: { children: React.ReactNode; className?: string }) {
  return <th className={`px-4 py-3 text-left font-semibold ${className ?? ""}`}>{children}</th>;
}
function Td({ children, className }: { children: React.ReactNode; className?: string }) {
  return <td className={`px-4 py-3 ${className ?? ""}`}>{children}</td>;
}
function Vazio({ mensagem }: { mensagem: string }) {
  return (
    <div className="card flex flex-col items-center justify-center px-6 py-12 text-center">
      <div className="mb-2 text-3xl">📭</div>
      <p className="text-sm text-ink-500">{mensagem}</p>
    </div>
  );
}

function CriarFuncionarioModal({
  apiFetch, onFechar, onCriado,
}: {
  apiFetch: (input: string, init?: RequestInit) => Promise<Response>;
  onFechar: () => void;
  onCriado: (u: Usuario) => void;
}) {
  const [perfil, setPerfil] = useState<Perfil>("RECEPCIONISTA");
  const [nome, setNome] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    if (senha.length < 6) {
      setErro("Senha precisa ter pelo menos 6 caracteres.");
      return;
    }
    setEnviando(true);
    try {
      const res = await apiFetch("/api/admin/funcionarios", {
        method: "POST",
        body: JSON.stringify({ perfil, nome, senhaInicial: senha }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha ao criar (HTTP ${res.status}).`);
      }
      onCriado(await asJson<Usuario>(res));
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="w-full max-w-md card p-6" onClick={e => e.stopPropagation()}>
        <header className="mb-5">
          <h3 className="text-lg font-bold text-ink-900">Cadastrar funcionário</h3>
          <p className="text-sm text-ink-500">A matrícula será gerada automaticamente.</p>
        </header>

        {erro && (
          <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        <form onSubmit={onSubmit} className="grid gap-4">
          <div>
            <label className="label">Perfil</label>
            <div className="grid grid-cols-2 gap-2">
              {(["RECEPCIONISTA", "MEDICO_VETERINARIO"] as const).map(p => (
                <button
                  type="button"
                  key={p}
                  onClick={() => setPerfil(p)}
                  className={
                    "rounded-xl border px-4 py-3 text-sm font-medium transition " +
                    (perfil === p
                      ? "border-brand-500 bg-brand-50 text-brand-700"
                      : "border-ink-300 text-ink-700 hover:bg-ink-100")
                  }
                >
                  {p === "RECEPCIONISTA" ? "Recepcionista" : "Médico Veterinário"}
                </button>
              ))}
            </div>
            <p className="mt-1 text-xs text-ink-500">
              {perfil === "RECEPCIONISTA"
                ? "Matrícula iniciará com 1 (ex.: 100002)."
                : "Matrícula iniciará com 2 (ex.: 200002)."}
            </p>
          </div>

          <div>
            <label className="label" htmlFor="nome">Nome completo</label>
            <input
              id="nome" required className="input"
              value={nome} onChange={e => setNome(e.target.value)}
            />
          </div>

          <div>
            <label className="label" htmlFor="senha">Senha inicial</label>
            <input
              id="senha" type="text" required minLength={6} className="input font-mono"
              placeholder="mínimo 6 caracteres"
              value={senha} onChange={e => setSenha(e.target.value)}
            />
            <p className="mt-1 text-xs text-ink-500">
              O funcionário usará essa senha no primeiro login (em produção, ele poderia
              trocar depois).
            </p>
          </div>

          <div className="mt-2 flex justify-end gap-2">
            <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">
              Cancelar
            </button>
            <button type="submit" disabled={enviando} className="btn-primary w-auto">
              {enviando ? "Criando…" : "Criar funcionário"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function TabelaPlanos({
  dados, onEditar, onExcluir,
}: {
  dados: Plano[];
  onEditar: (p: Plano) => void;
  onExcluir: (p: Plano) => void;
}) {
  if (dados.length === 0) {
    return <Vazio mensagem="Nenhum plano cadastrado ainda." />;
  }
  return (
    <div className="card overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-ink-100 text-xs uppercase tracking-wide text-ink-500">
          <tr>
            <Th>Nome</Th><Th>Mensalidade</Th><Th>Benefícios</Th><Th className="text-right">Ações</Th>
          </tr>
        </thead>
        <tbody className="divide-y divide-ink-300/40">
          {dados.map(p => (
            <tr key={p.id}>
              <Td><span className="font-medium text-ink-900">{p.nome}</span></Td>
              <Td>
                <span className="font-mono">
                  R$ {Number(p.valorMensalidade).toFixed(2).replace(".", ",")}
                </span>
                <span className="ml-1 text-ink-400">/mês</span>
              </Td>
              <Td>
                {p.beneficios.length === 0 ? (
                  <span className="text-ink-400">—</span>
                ) : (
                  <div className="flex flex-wrap gap-1">
                    {p.beneficios.map(b => (
                      <span
                        key={b.nome}
                        className="inline-flex items-center rounded-full bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700 ring-1 ring-brand-100"
                        title={`${b.limiteUsosPorPeriodo}x/${b.periodoRenovacao.toLowerCase()} · carência ${b.carenciaDias}d`}
                      >
                        {b.nome}: {b.limiteUsosPorPeriodo}x
                      </span>
                    ))}
                  </div>
                )}
              </Td>
              <Td className="text-right">
                <div className="inline-flex gap-2">
                  <button onClick={() => onEditar(p)} className="btn-ghost ring-1 ring-ink-300">
                    Editar
                  </button>
                  <BotaoExcluirPlano plano={p} onExcluir={onExcluir} />
                </div>
              </Td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function BotaoExcluirPlano({ plano, onExcluir }: { plano: Plano; onExcluir: (p: Plano) => void }) {
  const [confirmando, setConfirmando] = useState(false);
  const [excluindo, setExcluindo] = useState(false);

  if (!confirmando) {
    return (
      <button
        onClick={() => setConfirmando(true)}
        className="btn-ghost ring-1 ring-red-200 text-red-700 hover:bg-red-50"
      >
        Excluir
      </button>
    );
  }
  return (
    <span className="inline-flex items-center gap-1.5">
      <span className="text-xs font-medium text-red-700">Excluir?</span>
      <button onClick={() => setConfirmando(false)} disabled={excluindo}
        className="rounded-lg bg-white px-2 py-1 text-xs ring-1 ring-ink-300 hover:bg-ink-50">
        Não
      </button>
      <button
        onClick={async () => { setExcluindo(true); await onExcluir(plano); }}
        disabled={excluindo}
        className="rounded-lg bg-red-500 px-2 py-1 text-xs text-white hover:bg-red-600 disabled:opacity-60">
        {excluindo ? "…" : "Sim, excluir"}
      </button>
    </span>
  );
}

function PlanoModal({
  apiFetch, plano, onFechar, onSalvo,
}: {
  apiFetch: (input: string, init?: RequestInit) => Promise<Response>;
  plano: Plano | null;
  onFechar: () => void;
  onSalvo: (msg: string) => void;
}) {
  const editando = plano !== null;
  const [nome, setNome] = useState(plano?.nome ?? "");
  const [valor, setValor] = useState(
    plano ? String(Number(plano.valorMensalidade).toFixed(2)) : ""
  );
  // Parte dos benefícios fixos (Consulta, Vacinação). Em edição, mescla os
  // valores já configurados sobre os padrões para preservar nomes/ordem.
  const [beneficios, setBeneficios] = useState<Beneficio[]>(() =>
    BENEFICIOS_PADRAO.map(padrao => {
      const existente = plano?.beneficios.find(
        b => b.nome.toLowerCase() === padrao.nome.toLowerCase()
      );
      return existente ?? padrao;
    })
  );
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  function atualizarBeneficio(idx: number, patch: Partial<Beneficio>) {
    setBeneficios(prev => prev.map((b, i) => (i === idx ? { ...b, ...patch } : b)));
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    const valorNum = parseFloat(valor.replace(",", "."));
    if (isNaN(valorNum) || valorNum <= 0) {
      setErro("Informe um valor de mensalidade válido e maior que zero.");
      return;
    }
    if (beneficios.some(b => b.limiteUsosPorPeriodo < 0 || b.carenciaDias < 0)) {
      setErro("Usos e carência não podem ser negativos.");
      return;
    }
    setEnviando(true);
    try {
      const url = editando ? `/api/admin/planos/${plano.id}` : "/api/admin/planos";
      const method = editando ? "PUT" : "POST";
      const res = await apiFetch(url, {
        method,
        body: JSON.stringify({ nome, valorMensalidade: valorNum, beneficios }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha (HTTP ${res.status}).`);
      }
      onSalvo(editando ? `Plano "${nome}" atualizado` : `Plano "${nome}" criado`);
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto card p-6" onClick={e => e.stopPropagation()}>
        <header className="mb-5">
          <h3 className="text-lg font-bold text-ink-900">
            {editando ? "Editar plano" : "Novo plano"}
          </h3>
          <p className="text-sm text-ink-500">
            {editando
              ? "Altere a mensalidade e o que cada benefício oferece. Tutores ativos têm os limites sincronizados automaticamente."
              : "Defina a mensalidade e o que cada benefício oferece. O plano fica disponível para novas contratações."}
          </p>
        </header>

        {erro && (
          <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        <form onSubmit={onSubmit} className="grid gap-4">
          <div>
            <label className="label" htmlFor="p-nome">Nome do plano</label>
            <input
              id="p-nome" required className="input"
              placeholder="Ex.: Plano Premium Anual"
              value={nome} onChange={e => setNome(e.target.value)}
            />
          </div>
          <div>
            <label className="label" htmlFor="p-valor">Mensalidade (R$)</label>
            <input
              id="p-valor" required className="input font-mono"
              placeholder="0,00"
              value={valor} onChange={e => setValor(e.target.value)}
            />
            <p className="mt-1 text-xs text-ink-500">Use ponto ou vírgula como separador decimal.</p>
          </div>

          <div className="space-y-3">
            <p className="label mb-0">Benefícios do plano</p>
            {beneficios.map((b, idx) => (
              <BeneficioEditor
                key={b.nome}
                beneficio={b}
                onChange={patch => atualizarBeneficio(idx, patch)}
              />
            ))}
          </div>

          <div className="mt-2 flex justify-end gap-2">
            <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">
              Cancelar
            </button>
            <button type="submit" disabled={enviando} className="btn-primary w-auto">
              {enviando ? "Salvando…" : editando ? "Salvar alterações" : "Criar plano"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function BeneficioEditor({
  beneficio, onChange,
}: {
  beneficio: Beneficio;
  onChange: (patch: Partial<Beneficio>) => void;
}) {
  return (
    <div className="rounded-xl border border-ink-300/60 p-4">
      <p className="mb-3 text-sm font-semibold text-ink-800">{beneficio.nome}</p>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div>
          <label className="label text-xs">Usos por período</label>
          <input
            type="number" min={0} required className="input"
            value={beneficio.limiteUsosPorPeriodo}
            onChange={e => onChange({ limiteUsosPorPeriodo: Number(e.target.value) })}
          />
        </div>
        <div>
          <label className="label text-xs">Renovação</label>
          <select
            className="input"
            value={beneficio.periodoRenovacao}
            onChange={e => onChange({ periodoRenovacao: e.target.value as PeriodoRenovacao })}
          >
            {PERIODO_OPCOES.map(o => (
              <option key={o.valor} value={o.valor}>{o.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label text-xs">Carência (dias)</label>
          <input
            type="number" min={0} required className="input"
            value={beneficio.carenciaDias}
            onChange={e => onChange({ carenciaDias: Number(e.target.value) })}
          />
        </div>
      </div>
      <p className="mt-2 text-xs text-ink-500">
        {beneficio.limiteUsosPorPeriodo} uso(s) a cada{" "}
        {beneficio.periodoRenovacao === "MENSAL" ? "mês"
          : beneficio.periodoRenovacao === "TRIMESTRAL" ? "trimestre" : "ano"}
        {beneficio.carenciaDias > 0
          ? `, liberado ${beneficio.carenciaDias} dia(s) após a contratação.`
          : ", liberado imediatamente."}
      </p>
    </div>
  );
}

async function asJson<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({} as { mensagem?: string }));
    throw new Error(body?.mensagem ?? `HTTP ${res.status}`);
  }
  return (await res.json()) as T;
}
