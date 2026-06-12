import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { BrandWordmark } from "../../components/Brand";

const abas = [
  { to: "/app", label: "Início", end: true },
  { to: "/app/financeiro", label: "Financeiro" },
  { to: "/app/beneficios", label: "Benefícios" },
  { to: "/app/vacinacao", label: "Vacinação" },
  { to: "/app/nutricao", label: "Nutrição" },
  { to: "/app/medicamentos", label: "Remédios" },
  { to: "/app/agendamentos", label: "Consultas" },
  { to: "/app/conquistas", label: "Conquistas" },
  { to: "/app/indicacoes", label: "Indicações" },
];

export function TutorLayout() {
  const { session, logout } = useAuth();
  const navigate = useNavigate();

  function sair() {
    logout();
    navigate("/");
  }

  return (
    <div className="min-h-screen bg-ink-100/40">
      <header className="sticky top-0 z-30 border-b border-ink-300/60 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center gap-3 px-4 py-3">
          <BrandWordmark />
          <nav className="flex flex-1 flex-wrap items-center gap-0">
            {abas.map(aba => (
              <NavLink
                key={aba.to}
                to={aba.to}
                end={aba.end}
                className={({ isActive }) =>
                  "whitespace-nowrap rounded-lg px-2 py-2 text-sm font-medium transition " +
                  (isActive
                    ? "bg-brand-50 text-brand-700"
                    : "text-ink-700 hover:bg-ink-100")
                }
              >
                {aba.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-2">
            <span
              className="hidden max-w-[130px] truncate text-sm text-ink-500 2xl:inline"
              title={session?.user.nome ?? session?.user.identificador}
            >
              {primeiroEUltimoNome(session?.user.nome ?? session?.user.identificador)}
            </span>
            <button onClick={sair} className="btn-ghost ring-1 ring-ink-300">Sair</button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  );
}

/** "Felipe Marques Meira De Oliveira" → "Felipe Oliveira". Para nomes curtos, devolve o original. */
function primeiroEUltimoNome(nome: string | undefined): string {
  if (!nome) return "";
  const partes = nome.trim().split(/\s+/);
  if (partes.length <= 2) return nome;
  return `${partes[0]} ${partes[partes.length - 1]}`;
}
