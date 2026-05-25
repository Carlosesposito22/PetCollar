import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { PasswordInput } from "../components/PasswordInput";

type Form = {
  cpf: string;
  nomeCompleto: string;
  telefone: string;
  email: string;
  endereco: string;
  senha: string;
  confirmacao: string;
};

const inicial: Form = {
  cpf: "",
  nomeCompleto: "",
  telefone: "",
  email: "",
  endereco: "",
  senha: "",
  confirmacao: "",
};

export function ContratarPlano() {
  const navigate = useNavigate();
  const [form, setForm] = useState<Form>(inicial);
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  function update<K extends keyof Form>(k: K, v: string) {
    setForm(prev => ({ ...prev, [k]: v }));
  }

  function aplicarMascaraCpf(v: string) {
    const d = v.replace(/\D/g, "").slice(0, 11);
    return d
      .replace(/^(\d{3})(\d)/, "$1.$2")
      .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
      .replace(/\.(\d{3})(\d)/, ".$1-$2");
  }

  function aplicarMascaraTelefone(v: string) {
    const d = v.replace(/\D/g, "").slice(0, 11);
    if (d.length <= 10) return d.replace(/^(\d{2})(\d{4})(\d)/, "($1) $2-$3");
    return d.replace(/^(\d{2})(\d{5})(\d)/, "($1) $2-$3");
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);

    if (form.senha !== form.confirmacao) {
      setErro("As senhas não coincidem.");
      return;
    }
    if (form.senha.length < 6) {
      setErro("A senha deve ter pelo menos 6 caracteres.");
      return;
    }

    setEnviando(true);
    try {
      const res = await fetch("/api/tutores/contratar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          cpf: form.cpf,
          nomeCompleto: form.nomeCompleto,
          telefone: form.telefone,
          email: form.email.trim(),
          endereco: form.endereco,
          senha: form.senha,
        }),
      });

      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha ao contratar plano (HTTP ${res.status}).`);
      }

      const data = (await res.json()) as {
        identificador: string;
        nome: string;
        email: string;
        status: string;
        codigoPix: string;
      };

      navigate("/pagamento-pendente", {
        state: { tutor: data },
        replace: true,
      });
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <AuthLayout
      showBack
      highlight={
        <div className="mt-8 rounded-2xl bg-white/10 p-5 ring-1 ring-white/20 backdrop-blur">
          <p className="text-sm text-white/85">
            Contrate o plano, pague via PIX e libere o acesso para acompanhar consultas,
            vacinas e prescrições do seu pet.
          </p>
        </div>
      }
    >
      <div className="card p-8">
        <header className="mb-6">
          <span className="chip">Novo tutor</span>
          <h2 className="mt-3 text-2xl font-bold text-ink-900">Contratação de Plano</h2>
          <p className="mt-1 text-sm text-ink-500">
            Após a contratação, você receberá um QR Code PIX. O acesso é liberado quando
            o pagamento for confirmado.
          </p>
        </header>

        {erro && (
          <div role="alert" className="mb-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        <form onSubmit={onSubmit} noValidate className="grid gap-4">
          <Campo label="CPF" htmlFor="cpf">
            <input
              id="cpf" required className="input"
              placeholder="000.000.000-00" autoComplete="off"
              value={form.cpf}
              onChange={e => update("cpf", aplicarMascaraCpf(e.target.value))}
            />
          </Campo>

          <Campo label="Nome Completo" htmlFor="nome">
            <input
              id="nome" required className="input" autoComplete="name"
              value={form.nomeCompleto}
              onChange={e => update("nomeCompleto", e.target.value)}
            />
          </Campo>

          <Campo label="Telefone" htmlFor="telefone">
            <input
              id="telefone" required className="input"
              placeholder="(00) 00000-0000" autoComplete="tel"
              value={form.telefone}
              onChange={e => update("telefone", aplicarMascaraTelefone(e.target.value))}
            />
          </Campo>

          <Campo label="E-mail" htmlFor="email">
            <input
              id="email" type="email" required className="input"
              placeholder="seu@email.com" autoComplete="email"
              value={form.email}
              onChange={e => update("email", e.target.value)}
            />
          </Campo>

          <Campo label="Endereço" htmlFor="endereco">
            <textarea
              id="endereco" required className="input min-h-[88px]" rows={3}
              value={form.endereco}
              onChange={e => update("endereco", e.target.value)}
            />
          </Campo>

          <div className="grid gap-4 sm:grid-cols-2">
            <Campo label="Senha" htmlFor="senha">
              <PasswordInput
                id="senha" required autoComplete="new-password"
                placeholder="mínimo 6 caracteres"
                value={form.senha}
                onChange={e => update("senha", e.target.value)}
              />
            </Campo>
            <Campo label="Confirmar Senha" htmlFor="confirmacao">
              <PasswordInput
                id="confirmacao" required autoComplete="new-password"
                placeholder="repita a senha"
                value={form.confirmacao}
                onChange={e => update("confirmacao", e.target.value)}
              />
            </Campo>
          </div>

          <button type="submit" className="btn-primary mt-2" disabled={enviando}>
            {enviando ? "Processando…" : "Contratar Plano"}
          </button>
        </form>

        <p className="mt-6 text-xs text-ink-500">
          Após contratação, o sistema gera o QR Code automaticamente. O status muda de
          <strong> Pendente </strong> para <strong>Ativo</strong> após a confirmação do
          pagamento.
        </p>
      </div>
    </AuthLayout>
  );
}

function Campo({
  label, htmlFor, children,
}: { label: string; htmlFor: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="label" htmlFor={htmlFor}>{label}</label>
      {children}
    </div>
  );
}
