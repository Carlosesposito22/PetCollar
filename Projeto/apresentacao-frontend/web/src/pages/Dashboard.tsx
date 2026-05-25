import { useAuth } from "../auth/AuthContext";
import { BrandWordmark } from "../components/Brand";

const rotulos: Record<string, string> = {
  TUTOR: "Tutor",
  RECEPCIONISTA: "Recepcionista",
  MEDICO_VETERINARIO: "Médico Veterinário",
  ADMIN_CLINICA: "Administrador da clínica",
};

export function Dashboard() {
  const { session, logout } = useAuth();
  if (!session) return null;

  return (
    <div className="min-h-screen">
      <header className="border-b border-ink-300/60 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <BrandWordmark />
          <div className="flex items-center gap-4">
            <span className="chip">{rotulos[session.user.perfil] ?? session.user.perfil}</span>
            <span className="text-sm text-ink-700">{session.user.identificador}</span>
            <button onClick={logout} className="btn-ghost">Sair</button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-10">
        <h1 className="text-2xl font-bold text-ink-900">Sessão autenticada</h1>
        <p className="mt-2 text-ink-500">
          Bem-vindo! Você está autenticado como <strong>{rotulos[session.user.perfil]}</strong>.
          Em breve esta tela será substituída pelo painel do perfil correspondente.
        </p>

        <pre className="mt-6 overflow-auto rounded-xl bg-ink-900 p-4 text-xs text-brand-100">
{JSON.stringify(session, null, 2)}
        </pre>
      </main>
    </div>
  );
}
