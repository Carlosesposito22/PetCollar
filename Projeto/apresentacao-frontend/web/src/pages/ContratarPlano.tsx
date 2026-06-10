import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { PasswordInput } from "../components/PasswordInput";

type Beneficio = {
  nome: string;
  periodoRenovacao: "MENSAL" | "TRIMESTRAL" | "ANUAL";
  limiteUsosPorPeriodo: number;
  carenciaDias: number;
};

type Plano = {
  id: string;
  nome: string;
  valorMensalidade: number;
  beneficios: Beneficio[];
};

const PERIODO_ABREV: Record<Beneficio["periodoRenovacao"], string> = {
  MENSAL: "mês",
  TRIMESTRAL: "trim.",
  ANUAL: "ano",
};

type Form = {
  cpf: string;
  nomeCompleto: string;
  telefone: string;
  email: string;
  endereco: string;
  senha: string;
  confirmacao: string;
  planoId: string;
};

const inicial: Form = {
  cpf: "",
  nomeCompleto: "",
  telefone: "",
  email: "",
  endereco: "",
  senha: "",
  confirmacao: "",
  planoId: "",
};

export function ContratarPlano() {
  const navigate = useNavigate();
  const [form, setForm] = useState<Form>(inicial);
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [planos, setPlanos] = useState<Plano[]>([]);
  const [carregandoPlanos, setCarregandoPlanos] = useState(true);

  useEffect(() => {
    fetch("/api/planos")
      .then(r => r.json())
      .then((dados: Plano[]) => {
        setPlanos(dados);
        if (dados.length > 0) {
          setForm(prev => ({ ...prev, planoId: dados[0].id }));
        }
      })
      .catch(() => {
        // mantém lista vazia; usuário pode tentar novamente
      })
      .finally(() => setCarregandoPlanos(false));
  }, []);

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

    if (!form.planoId) {
      setErro("Selecione um plano para continuar.");
      return;
    }
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
          planoId: form.planoId,
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

  const planoSelecionado = planos.find(p => p.id === form.planoId);

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

          {/* Seleção de plano */}
          <div>
            <label className="label">Plano</label>
            {carregandoPlanos ? (
              <p className="text-sm text-ink-400">Carregando planos…</p>
            ) : planos.length === 0 ? (
              <p className="text-sm text-amber-700">Nenhum plano disponível no momento.</p>
            ) : (
              <div className="grid gap-2 sm:grid-cols-2">
                {planos.map(p => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => update("planoId", p.id)}
                    className={
                      "rounded-xl border px-4 py-3 text-left transition " +
                      (form.planoId === p.id
                        ? "border-brand-500 bg-brand-50 text-brand-700"
                        : "border-ink-300 text-ink-700 hover:bg-ink-50")
                    }
                  >
                    <p className="font-medium text-sm">{p.nome}</p>
                    <p className="mt-0.5 text-xs opacity-75">
                      R$ {Number(p.valorMensalidade).toFixed(2).replace(".", ",")}/mês
                    </p>
                    {p.beneficios && p.beneficios.length > 0 && (
                      <ul className="mt-2 space-y-0.5 text-xs opacity-80">
                        {p.beneficios.map(b => (
                          <li key={b.nome}>
                            • {b.nome}: {b.limiteUsosPorPeriodo}x/{PERIODO_ABREV[b.periodoRenovacao]}
                            {b.carenciaDias > 0 ? ` · carência ${b.carenciaDias}d` : ""}
                          </li>
                        ))}
                      </ul>
                    )}
                  </button>
                ))}
              </div>
            )}
            {planoSelecionado && (
              <p className="mt-1.5 text-xs text-ink-500">
                Plano selecionado: <strong>{planoSelecionado.nome}</strong> —{" "}
                R$ {Number(planoSelecionado.valorMensalidade).toFixed(2).replace(".", ",")}/mês
              </p>
            )}
          </div>

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

          <button type="submit" className="btn-primary mt-2" disabled={enviando || carregandoPlanos}>
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
