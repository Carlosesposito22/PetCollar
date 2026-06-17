import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { BrandWordmark } from "../../components/Brand";
import { useMeuProtocoloAtivo } from "../../features/protocolo-inacessibilidade/hooks/useMeuProtocoloAtivo";
import { isProtocoloAtivo } from "../../features/protocolo-inacessibilidade/tipos";

const ABAS_NAV = [
  { to: "/app", label: "Início", end: true },
  { to: "/app/financeiro", label: "Financeiro" },
  { to: "/app/beneficios", label: "Benefícios" },
  { to: "/app/vacinacao", label: "Vacinação" },
  { to: "/app/nutricao", label: "Nutrição" },
  { to: "/app/medicamentos", label: "Medicamentos" },
  { to: "/app/agendamentos", label: "Consultas" },
  { to: "/app/conquistas", label: "Conquistas" },
  { to: "/app/indicacoes", label: "Indicações" },
];

export function TutorLayout() {
  const { session, logout } = useAuth();
  const navigate = useNavigate();
  const { dados } = useMeuProtocoloAtivo();
  const protocoloAtivo = dados != null && isProtocoloAtivo(dados.status);

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
            {ABAS_NAV.map(aba => (
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

            {/* Avisos — destaque pulsante quando há protocolo ativo */}
            <NavLink
              to="/app/avisos"
              className={({ isActive }) =>
                "relative flex items-center gap-1.5 whitespace-nowrap rounded-lg px-2 py-2 text-sm font-medium transition " +
                (isActive ? "bg-amber-50 text-amber-700" : "text-ink-700 hover:bg-ink-100")
              }
            >
              📣 Avisos
              {protocoloAtivo && (
                <span className="relative flex h-2.5 w-2.5">
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-amber-400 opacity-75" />
                  <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-amber-500" />
                </span>
              )}
            </NavLink>
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

        {/* Banner persistente quando protocolo ativo — visível em qualquer página do tutor */}
        {protocoloAtivo && (
          <div className="border-t border-amber-200 bg-amber-50 px-4 py-2">
            <p className="mx-auto max-w-7xl text-sm font-medium text-amber-800">
              📣 A clínica está tentando entrar em contato com você.{" "}
              <NavLink to="/app/avisos" className="underline hover:text-amber-900">
                Ver aviso e confirmar presença →
              </NavLink>
            </p>
          </div>
        )}
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
