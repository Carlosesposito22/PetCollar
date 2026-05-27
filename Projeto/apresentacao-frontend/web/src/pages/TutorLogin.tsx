import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { PasswordInput } from "../components/PasswordInput";
import { LoginError, useAuth } from "../auth/AuthContext";

export function TutorLogin() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [suspensa, setSuspensa] = useState(false);
  const [pendenteId, setPendenteId] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setSuspensa(false);
    setPendenteId(null);
    setEnviando(true);
    try {
      await login("TUTOR", email.trim(), senha);
      navigate("/app");
    } catch (err) {
      if (err instanceof LoginError) {
        if (err.code === "CONTA_SUSPENSA") setSuspensa(true);
        else if (err.code === "PAGAMENTO_PENDENTE") setPendenteId(err.contaId ?? email.trim());
        else setErro(err.message);
      } else {
        setErro((err as Error).message);
      }
    } finally {
      setEnviando(false);
    }
  }

  async function irParaPagamento() {
    if (!pendenteId) return;
    navigate("/pagamento-pendente", {
      state: {
        tutor: {
          identificador: pendenteId,
          nome: pendenteId,
          email: pendenteId,
          status: "PENDENTE",
          codigoPix: "00020126580014BR.GOV.BCB.PIX0136a1b2c3d4...",
        },
      },
    });
  }

  return (
    <AuthLayout showBack>
      <div className="card p-8">
        <header className="mb-6">
          <span className="chip">Perfil: Tutor</span>
          <h2 className="mt-3 text-2xl font-bold text-ink-900">Login do Tutor</h2>
          <p className="mt-1 text-sm text-ink-500">
            Acesse com seu e-mail e senha cadastrados no plano.
          </p>
        </header>

        {suspensa && (
          <div
            role="alert"
            className="mb-5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
          >
            <strong className="block font-semibold">Conta suspensa</strong>
            Seu acesso está bloqueado. Regularize sua assinatura ou entre em contato com o suporte.
          </div>
        )}

        {pendenteId && (
          <div role="alert" className="mb-5 rounded-xl border border-amber-300 bg-amber-50 p-4 text-sm text-amber-900">
            <strong className="block font-semibold">Pagamento pendente</strong>
            Sua conta foi criada, mas o acesso só é liberado após a confirmação do pagamento.
            <button
              type="button"
              onClick={irParaPagamento}
              className="mt-3 inline-flex rounded-lg bg-amber-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-amber-800"
            >
              Ver QR Code de Pagamento →
            </button>
          </div>
        )}

        {erro && !suspensa && !pendenteId && (
          <div role="alert" className="mb-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        <form onSubmit={onSubmit} noValidate>
          <div className="mb-4">
            <label htmlFor="email" className="label">E-mail</label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              required
              placeholder="seu@email.com"
              className="input"
              value={email}
              onChange={e => setEmail(e.target.value)}
            />
          </div>

          <div className="mb-2">
            <label htmlFor="senha" className="label">Senha</label>
            <PasswordInput
              id="senha"
              autoComplete="current-password"
              required
              placeholder="••••••••"
              value={senha}
              onChange={e => setSenha(e.target.value)}
            />
          </div>

          <div className="mb-6 flex items-center justify-between text-xs">
            <label className="inline-flex items-center gap-2 text-ink-500">
              <input type="checkbox" className="h-4 w-4 rounded border-ink-300 text-brand-600 focus:ring-brand-500" />
              Lembrar de mim
            </label>
            <button type="button" className="font-medium text-brand-700 hover:underline">
              Esqueci minha senha
            </button>
          </div>

          <button type="submit" className="btn-primary" disabled={enviando}>
            {enviando ? "Entrando…" : "Entrar"}
          </button>
        </form>

        <div className="my-6 flex items-center gap-3 text-xs text-ink-500">
          <div className="h-px flex-1 bg-ink-300/60" />
          ou
          <div className="h-px flex-1 bg-ink-300/60" />
        </div>

        <Link to="/contratar-plano" className="btn-ghost w-full justify-center ring-1 ring-ink-300 hover:ring-brand-500">
          Ainda não tenho conta → Contratar Plano
        </Link>
      </div>

      <p className="mt-4 text-center text-xs text-ink-500">
        Se a sua conta estiver suspensa ou aguardando pagamento, exibiremos um aviso aqui bloqueando o acesso.
      </p>
    </AuthLayout>
  );
}
