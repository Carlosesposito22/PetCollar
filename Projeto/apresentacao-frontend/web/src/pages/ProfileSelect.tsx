import { useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";

type Perfil = {
  id: "tutor" | "recepcionista" | "medico";
  titulo: string;
  descricao: string;
  rota: string;
  icone: JSX.Element;
};

const perfis: Perfil[] = [
  {
    id: "tutor",
    titulo: "Tutor",
    descricao: "Acompanhe consultas, vacinas e prescrições do seu pet.",
    rota: "/login/tutor",
    icone: <IconUser />,
  },
  {
    id: "recepcionista",
    titulo: "Recepcionista",
    descricao: "Recepção, cadastro e triagem inicial do paciente.",
    rota: "/login/recepcionista",
    icone: <IconClipboard />,
  },
  {
    id: "medico",
    titulo: "Médico Veterinário",
    descricao: "Atendimento clínico, plano nutricional e prescrição.",
    rota: "/login/medico",
    icone: <IconStethoscope />,
  },
];

export function ProfileSelect() {
  const navigate = useNavigate();
  return (
    <AuthLayout
      highlight={
        <div className="mt-8 rounded-2xl bg-white/10 p-5 ring-1 ring-white/20 backdrop-blur">
          <p className="text-sm text-white/85">
            “Cada perfil tem um fluxo dedicado — escolha como você quer entrar e o
            sistema já te leva para o painel certo.”
          </p>
        </div>
      }
    >
      <header className="mb-8">
        <span className="chip">Bem-vindo</span>
        <h2 className="mt-3 text-3xl font-bold tracking-tight text-ink-900">
          Selecione seu perfil de acesso
        </h2>
        <p className="mt-2 text-sm text-ink-500">
          Você será direcionado para a tela de login correspondente.
        </p>
      </header>

      <div className="grid gap-3">
        {perfis.map(p => (
          <button
            key={p.id}
            onClick={() => navigate(p.rota)}
            className="group card flex items-center gap-4 p-5 text-left transition hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-brand-100"
          >
            <span className="inline-flex h-12 w-12 flex-none items-center justify-center rounded-xl bg-brand-50 text-brand-700 ring-1 ring-brand-100 group-hover:bg-brand-100">
              {p.icone}
            </span>
            <span className="flex-1">
              <span className="block text-base font-semibold text-ink-900">{p.titulo}</span>
              <span className="block text-sm text-ink-500">{p.descricao}</span>
            </span>
            <ChevronRight />
          </button>
        ))}
      </div>

      <p className="mt-8 text-xs text-ink-500">
        Cada card redireciona para a tela de login específica da persona.
      </p>

      <div className="mt-6 border-t border-ink-300/50 pt-4 text-center">
        <button
          onClick={() => navigate("/login/admin")}
          className="text-xs font-medium text-ink-500 hover:text-brand-700"
        >
          Sou administrador da clínica →
        </button>
      </div>
    </AuthLayout>
  );
}

function ChevronRight() {
  return (
    <svg className="text-ink-300 transition group-hover:translate-x-0.5 group-hover:text-brand-600" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 18l6-6-6-6" />
    </svg>
  );
}
function IconUser() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  );
}
function IconClipboard() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="8" y="2" width="8" height="4" rx="1" />
      <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
      <path d="M9 12h6M9 16h6" />
    </svg>
  );
}
function IconStethoscope() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 2v6a4 4 0 0 0 8 0V2" />
      <path d="M6 8v2a6 6 0 0 0 12 0V8" />
      <circle cx="20" cy="14" r="2" />
      <path d="M18 16v2a4 4 0 0 1-8 0v-2" />
    </svg>
  );
}
