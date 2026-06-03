import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { formatarCompetencia, formatarData, formatarReal } from "../../utils/formato";

type StatusConta = "PENDENTE" | "ATIVA" | "INADIMPLENTE" | "SUSPENSA";
type StatusMensalidade = "PENDENTE" | "EM_ATRASO" | "PAGO";

type Plano = { nome: string; valor: number };

type Mensalidade = {
  id: string;
  competencia: string;        // "yyyy-MM"
  valorOriginal: number;
  descontoIndicacao: number;
  juros: number;
  valorAtualizado: number;
  vencimento: string;
  dataPagamento: string | null;
  diasAtraso: number;
  status: StatusMensalidade;
};

type Resumo = {
  plano: Plano;
  statusConta: StatusConta;
  proximoVencimento: string | null;
  mensalidades: Mensalidade[];
};

export function TutorFinanceiro() {
  const { apiFetch } = useAuth();
  const navigate = useNavigate();
  const [resumo, setResumo] = useState<Resumo | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setErro(null);
    setCarregando(true);
    try {
      const res = await apiFetch("/api/tutor/financeiro");
      if (!res.ok) throw new Error(`Falha ao carregar (HTTP ${res.status}).`);
      setResumo(await res.json());
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  useEffect(() => { void carregar(); }, [carregar]);

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-ink-900">Área Financeira</h1>

      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
          {erro}
        </div>
      )}

      {carregando && <p className="text-sm text-ink-500">Carregando…</p>}

      {resumo && !carregando && (
        <>
          <CartaoResumo resumo={resumo} />
          <TabelaMensalidades
            mensalidades={resumo.mensalidades}
            onPagar={m => navigate(`/app/financeiro/pagamento/${m.id}`)}
          />
          <Anotacao />
        </>
      )}
    </div>
  );
}

function CartaoResumo({ resumo }: { resumo: Resumo }) {
  return (
    <section className="card mb-6 p-6 ring-1 ring-ink-300/60">
      <div className="grid gap-6 sm:grid-cols-3">
        <Bloco rotulo="Plano Contratado">
          <p className="text-base font-semibold text-ink-900">{resumo.plano.nome}</p>
          <p className="mt-0.5 text-xs text-ink-500">
            {formatarReal(resumo.plano.valor)} / mês
          </p>
        </Bloco>
        <Bloco rotulo="Status da Conta">
          <StatusContaBadge status={resumo.statusConta} />
        </Bloco>
        <Bloco rotulo="Próximo Vencimento">
          <p className="text-base font-semibold text-ink-900">
            {formatarData(resumo.proximoVencimento)}
          </p>
        </Bloco>
      </div>
    </section>
  );
}

function Bloco({ rotulo, children }: { rotulo: string; children: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-ink-500">{rotulo}</p>
      <div className="mt-1">{children}</div>
    </div>
  );
}

function StatusContaBadge({ status }: { status: StatusConta }) {
  const map: Record<StatusConta, { cor: string; rotulo: string }> = {
    PENDENTE:     { cor: "text-amber-700",  rotulo: "Pendente" },
    ATIVA:        { cor: "text-brand-600",  rotulo: "Ativa" },
    INADIMPLENTE: { cor: "text-orange-600", rotulo: "Inadimplente" },
    SUSPENSA:     { cor: "text-paw-600",    rotulo: "Suspensa" },
  };
  const s = map[status];
  return <p className={`text-base font-semibold ${s.cor}`}>{s.rotulo}</p>;
}

function TabelaMensalidades({
  mensalidades, onPagar,
}: {
  mensalidades: Mensalidade[];
  onPagar: (m: Mensalidade) => void;
}) {
  if (mensalidades.length === 0) {
    return (
      <div className="card px-6 py-10 text-center text-sm text-ink-500">
        Você ainda não possui mensalidades registradas.
      </div>
    );
  }

  return (
    <div className="card mb-6 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-ink-100 text-left text-xs font-semibold uppercase tracking-wide text-ink-700">
            <tr>
              <Th>Competência</Th>
              <Th>Valor Original</Th>
              <Th>Desc. Indicação</Th>
              <Th>Juros</Th>
              <Th>Valor Atualizado</Th>
              <Th>Vencimento</Th>
              <Th>Data Pagamento</Th>
              <Th>Status</Th>
              <Th className="text-right">Ação</Th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-300/40">
            {mensalidades.map(m => (
              <tr key={m.id} className="hover:bg-ink-100/40">
                <Td>{formatarCompetencia(m.competencia)}</Td>
                <Td>{formatarReal(m.valorOriginal)}</Td>
                <Td>{m.descontoIndicacao > 0 ? formatarReal(m.descontoIndicacao) : "—"}</Td>
                <Td>{formatarReal(m.juros)}</Td>
                <Td className="font-semibold text-ink-900">{formatarReal(m.valorAtualizado)}</Td>
                <Td>{formatarData(m.vencimento)}</Td>
                <Td>{formatarData(m.dataPagamento)}</Td>
                <Td><StatusMensalidadeBadge status={m.status} /></Td>
                <Td className="text-right">
                  {m.status !== "PAGO" && (
                    <button
                      onClick={() => onPagar(m)}
                      className="btn-ghost ring-1 ring-ink-300 hover:ring-brand-500"
                    >
                      Pagar
                    </button>
                  )}
                </Td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function Th({ children, className }: { children: React.ReactNode; className?: string }) {
  return <th className={`px-4 py-3 ${className ?? ""}`}>{children}</th>;
}
function Td({ children, className }: { children: React.ReactNode; className?: string }) {
  return <td className={`px-4 py-3 ${className ?? ""}`}>{children}</td>;
}

function StatusMensalidadeBadge({ status }: { status: StatusMensalidade }) {
  const map: Record<StatusMensalidade, string> = {
    PAGO:      "text-brand-600",
    PENDENTE:  "text-amber-700",
    EM_ATRASO: "text-paw-600",
  };
  const rotulo: Record<StatusMensalidade, string> = {
    PAGO: "Pago",
    PENDENTE: "Pendente",
    EM_ATRASO: "Em Atraso",
  };
  return <span className={`font-semibold ${map[status]}`}>{rotulo[status]}</span>;
}

function Anotacao() {
  return (
    <div className="rounded-xl border border-dashed border-ink-300 bg-white/60 p-4 text-xs leading-relaxed text-ink-500">
      Juros calculados automaticamente (0,033% ao dia). Conta <strong>Suspensa</strong> após 3
      mensalidades consecutivas em atraso. QR Code gerado dinamicamente com valor atualizado.
      Descontos de indicação aplicados automaticamente ao confirmar pagamento do indicado.
    </div>
  );
}
