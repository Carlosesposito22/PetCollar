import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { PasswordInput } from "../components/PasswordInput";
import { LoginError, useAuth } from "../auth/AuthContext";

export function AdminLogin() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [identificador, setIdentificador] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await login("ADMIN_CLINICA", identificador.trim(), senha);
      navigate("/admin");
    } catch (err) {
      if (err instanceof LoginError) setErro(err.message);
      else setErro((err as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <AuthLayout showBack>
      <div className="card p-8">
        <div className="mb-6 flex items-center justify-between">
          <span className="chip">Perfil: Admin</span>
          <span className="text-xs text-ink-500">Acesso restrito</span>
        </div>

        <header className="mb-6">
          <h2 className="text-2xl font-bold text-ink-900">Login do Administrador</h2>
          <p className="mt-1 text-sm text-ink-500">
            Acesso reservado ao gestor da clínica. Aqui você cadastra funcionários e
            administra contas.
          </p>
        </header>

        {erro && (
          <div role="alert" className="mb-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        <form onSubmit={onSubmit} noValidate>
          <div className="mb-4">
            <label htmlFor="ident" className="label">E-mail ou matrícula</label>
            <input
              id="ident" required autoComplete="username" className="input"
              placeholder="admin@petcollar.com"
              value={identificador}
              onChange={e => setIdentificador(e.target.value)}
            />
          </div>

          <div className="mb-6">
            <label htmlFor="senha" className="label">Senha</label>
            <PasswordInput
              id="senha" required autoComplete="current-password"
              placeholder="••••••••"
              value={senha}
              onChange={e => setSenha(e.target.value)}
            />
          </div>

          <button type="submit" className="btn-primary" disabled={enviando}>
            {enviando ? "Entrando…" : "Entrar"}
          </button>
        </form>
      </div>

      <p className="mt-4 text-center text-xs text-ink-500">
        Dica de demo: <code className="rounded bg-ink-100 px-1.5 py-0.5">admin@petcollar.com</code>
        {" / "}
        <code className="rounded bg-ink-100 px-1.5 py-0.5">petcollar123</code>
      </p>
    </AuthLayout>
  );
}
