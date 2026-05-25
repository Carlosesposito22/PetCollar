import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { PasswordInput } from "../components/PasswordInput";
import { useAuth, type Perfil } from "../auth/AuthContext";

type Props = {
  perfil: Perfil;
  rotuloPerfil: string;
  titulo: string;
};

export function MatriculaLogin({ perfil, rotuloPerfil, titulo }: Props) {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [matricula, setMatricula] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  function onMatriculaChange(v: string) {
    const apenasDigitos = v.replace(/\D/g, "").slice(0, 6);
    setMatricula(apenasDigitos);
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await login(perfil, matricula, senha);
      navigate("/app");
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <AuthLayout showBack>
      <div className="card p-8">
        <div className="mb-6 flex items-center justify-between">
          <span className="chip">Perfil: {rotuloPerfil}</span>
          <span className="text-xs text-ink-500">Acesso interno</span>
        </div>

        <header className="mb-6">
          <h2 className="text-2xl font-bold text-ink-900">{titulo}</h2>
          <p className="mt-1 text-sm text-ink-500">
            Use sua matrícula corporativa e senha para acessar o painel.
          </p>
        </header>

        {erro && (
          <div role="alert" className="mb-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        <form onSubmit={onSubmit} noValidate>
          <div className="mb-4">
            <label htmlFor="matricula" className="label">Matrícula</label>
            <input
              id="matricula"
              inputMode="numeric"
              autoComplete="username"
              required
              minLength={6}
              maxLength={6}
              pattern="\d{6}"
              placeholder="000000"
              className="input tracking-[0.4em]"
              value={matricula}
              onChange={e => onMatriculaChange(e.target.value)}
            />
            <p className="mt-1 text-xs text-ink-500">6 dígitos numéricos.</p>
          </div>

          <div className="mb-6">
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

          <button type="submit" className="btn-primary" disabled={enviando || matricula.length !== 6}>
            {enviando ? "Entrando…" : "Entrar"}
          </button>
        </form>

        <p className="mt-6 text-xs text-ink-500">
          Problemas para acessar? Procure o administrador da clínica.
        </p>
      </div>

      <p className="mt-4 text-center text-xs text-ink-500">
        Label indica a persona selecionada no topo do cartão.
      </p>
    </AuthLayout>
  );
}
