import { useCallback, useEffect, useState } from "react";
import { useAuth, type Perfil } from "../auth/AuthContext";
import { BrandWordmark } from "../components/Brand";

type Status = "PENDENTE" | "ATIVA" | "INADIMPLENTE" | "SUSPENSA";

type Usuario = {
  identificador: string;
  nome: string;
  perfil: Perfil;
  status: Status;
  email?: string | null;
  telefone?: string | null;
};

type Aba = "funcionarios" | "tutores";

export function AdminPanel() {
  const { session, logout, apiFetch } = useAuth();
  const [aba, setAba] = useState<Aba>("funcionarios");
  const [funcionarios, setFuncionarios] = useState<Usuario[]>([]);
  const [tutores, setTutores] = useState<Usuario[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [criandoAberto, setCriandoAberto] = useState(false);

  const recarregar = useCallback(async () => {
    setErro(null);
    try {
      const [f, t] = await Promise.all([
        apiFetch("/api/admin/funcionarios").then(asJson<Usuario[]>),
        apiFetch("/api/admin/tutores").then(asJson<Usuario[]>),
      ]);
      setFuncionarios(f);
      setTutores(t);
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
    <div className="min-h-screen">
      <header className="border-b border-ink-300/60 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <BrandWordmark />
          <div className="flex items-center gap-4">
            <span className="chip">Admin da clínica</span>
            <span className="text-sm text-ink-700">{session?.user.identificador}</span>
            <button onClick={logout} className="btn-ghost">Sair</button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-10">
        <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold text-ink-900">Gestão da clínica</h1>
            <p className="text-sm text-ink-500">
              Cadastre funcionários e gerencie tutores. Matrículas são geradas automaticamente.
            </p>
          </div>
          {aba === "funcionarios" && (
            <button onClick={() => setCriandoAberto(true)} className="btn-primary w-auto">
              + Cadastrar funcionário
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
        ) : (
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
    </div>
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

async function asJson<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({} as { mensagem?: string }));
    throw new Error(body?.mensagem ?? `HTTP ${res.status}`);
  }
  return (await res.json()) as T;
}
