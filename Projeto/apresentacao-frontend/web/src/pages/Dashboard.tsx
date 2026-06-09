import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export function Dashboard() {
  const { session } = useAuth();
  const navigate = useNavigate();

  if (!session) return null;

  const isRecepcionista = session.user.perfil === "RECEPCIONISTA";

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <div className="mb-10 flex flex-col gap-4 rounded-3xl bg-white p-8 shadow-card">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">Painel da Recepção</p>
          <h1 className="mt-3 text-3xl font-bold text-ink-900">Painel da Recepcionista</h1>
          <p className="mt-2 max-w-2xl text-sm text-ink-500">
            Área de trabalho inicial para acessar os recursos principais da recepção de forma rápida e intuitiva.
          </p>
        </div>
      </div>

      {isRecepcionista ? (
        <>
          <div className="grid gap-4 md:grid-cols-2">
            <button type="button" onClick={() => navigate("/recepcao/busca-tutor")}
              className="group card flex min-h-[240px] flex-col justify-between rounded-[28px] border border-ink-200/80 bg-white p-8 text-left transition hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-brand-100">
              <span className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-50 text-brand-700">
                <IconSearch />
              </span>
              <div>
                <h2 className="text-xl font-semibold text-ink-900">Buscar Tutor por CPF</h2>
                <p className="mt-3 text-sm leading-6 text-ink-500">
                  Encontre o tutor e os animais vinculados pelo CPF.
                  Inclui alerta epidemiológico automático e triagem clínica.
                </p>
              </div>
              <span className="text-sm font-medium text-brand-700">Abrir busca →</span>
            </button>

            <button type="button" onClick={() => navigate("/recepcao/fila-espera")}
              className="group card flex min-h-[240px] flex-col justify-between rounded-[28px] border border-ink-200/80 bg-white p-8 text-left transition hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-brand-100">
              <span className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-50 text-brand-700">
                <IconQueue />
              </span>
              <div>
                <h2 className="text-xl font-semibold text-ink-900">Visualizar Fila de Espera Dinâmica</h2>
                <p className="mt-3 text-sm leading-6 text-ink-500">
                  Veja a fila atualizada em tempo real para organizar os atendimentos com mais agilidade.
                </p>
              </div>
              <span className="text-sm font-medium text-brand-700">Abrir fila →</span>
            </button>
          </div>

          <div className="mt-8 rounded-3xl border border-dashed border-ink-300/70 bg-white/90 p-6 text-sm text-ink-600">
            ANOTAÇÃO: Painel de acesso rápido às funções principais da recepção
          </div>
        </>
      ) : (
        <div className="card rounded-3xl p-8">
          <h2 className="text-xl font-semibold text-ink-900">Acesso de staff</h2>
          <p className="mt-3 text-sm text-ink-500">
            Você está autenticado como <strong>{session.user.perfil}</strong>.
          </p>
        </div>
      )}
    </main>
  );
}

function IconSearch() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="7" />
      <path d="m21 21-4.3-4.3" />
    </svg>
  );
}

function IconQueue() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="4" rx="1" />
      <rect x="3" y="10" width="12" height="4" rx="1" />
      <rect x="3" y="16" width="8" height="4" rx="1" />
    </svg>
  );
}