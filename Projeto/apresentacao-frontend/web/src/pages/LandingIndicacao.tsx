import { useNavigate, useParams } from "react-router-dom";

export function LandingIndicacao() {
  const { codigo } = useParams<{ codigo: string }>();
  const navigate = useNavigate();

  function irParaContratacao() {
    navigate(`/contratar-plano?indicacao=${codigo ?? ""}`);
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-brand-600 to-brand-800 flex flex-col items-center justify-center px-4 py-12">
      <div className="w-full max-w-2xl">
        {/* Logo / título */}
        <div className="text-center mb-10">
          <span className="text-4xl font-extrabold text-white tracking-tight">
            pet<span className="text-brand-200">Collar</span>
          </span>
          <p className="mt-2 text-brand-100 text-lg">
            Saúde do seu pet, sempre ao alcance das suas mãos.
          </p>
        </div>

        {/* Banner de indicação */}
        <div className="rounded-2xl bg-white/10 ring-1 ring-white/20 backdrop-blur p-6 mb-8 text-center">
          <p className="text-white/70 text-sm uppercase tracking-widest mb-1">Você foi convidado</p>
          <p className="text-white font-semibold text-lg">
            Um tutor petCollar indicou você. Assine agora e ganhe{" "}
            <span className="text-brand-200 font-bold">30% de desconto</span> na primeira mensalidade!
          </p>
        </div>

        {/* Benefícios */}
        <div className="grid gap-4 sm:grid-cols-2 mb-10">
          {BENEFICIOS.map(b => (
            <div key={b.titulo} className="rounded-xl bg-white/10 ring-1 ring-white/20 p-5 flex gap-4 items-start">
              <span className="text-2xl">{b.icone}</span>
              <div>
                <p className="text-white font-semibold text-sm">{b.titulo}</p>
                <p className="text-white/70 text-xs mt-0.5">{b.descricao}</p>
              </div>
            </div>
          ))}
        </div>

        {/* CTA */}
        <div className="text-center">
          <button
            onClick={irParaContratacao}
            className="inline-flex items-center gap-2 rounded-2xl bg-white px-10 py-4 text-brand-700 font-bold text-base shadow-xl hover:bg-brand-50 transition-colors"
          >
            Assinar agora com desconto
          </button>
          <p className="mt-3 text-white/50 text-xs">
            Código de indicação: <span className="font-mono text-white/70">{codigo}</span>
          </p>
        </div>
      </div>
    </div>
  );
}

const BENEFICIOS = [
  {
    icone: "🩺",
    titulo: "Consultas incluídas",
    descricao: "Acesso a consultas veterinárias sem custo adicional dentro do plano.",
  },
  {
    icone: "💉",
    titulo: "Vacinação em dia",
    descricao: "Acompanhe e agende vacinas diretamente pelo app.",
  },
  {
    icone: "📋",
    titulo: "Prontuário digital",
    descricao: "Histórico completo de saúde do seu pet, sempre acessível.",
  },
  {
    icone: "🏆",
    titulo: "Gamificação e recompensas",
    descricao: "Ganhe conquistas e benefícios ao cuidar da saúde do seu pet.",
  },
];
