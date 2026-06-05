import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { formatarCompetencia, formatarData, formatarReal } from "../../utils/formato";

// Espelham os enums do domínio (dominio-AssinaturaFaturamento + servico).
type SituacaoConta = "PENDENTE" | "ATIVA" | "INADIMPLENTE" | "SUSPENSA";
type StatusCobranca = "PENDENTE" | "EM_ATRASO" | "PAGA";

type Plano = { id: string; nome: string; valor: number };

type Cobranca = {
  id: string;
  competencia: string;        // "yyyy-MM"
  valorOriginal: number;
  descontoIndicacao: number;
  juros: number;
  valorAtualizado: number;
  vencimento: string;
  dataPagamento: string | null;
  diasAtraso: number;
  status: StatusCobranca;
};

type Resumo = {
  plano: Plano | null;
  statusConta: SituacaoConta;
  proximoVencimento: string | null;
  cobrancas: Cobranca[];
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
          <TabelaCobrancas
            cobrancas={resumo.cobrancas}
            onPagar={c => navigate(`/app/financeiro/pagamento/${c.id}`)}
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
          {resumo.plano ? (
            <>
              <p className="text-base font-semibold text-ink-900">{resumo.plano.nome}</p>
              <p className="mt-0.5 text-xs text-ink-500">
                {formatarReal(resumo.plano.valor)} / mês
              </p>
            </>
          ) : (
            <p className="text-base text-ink-500">—</p>
          )}
        </Bloco>
        <Bloco rotulo="Status da Conta">
          <SituacaoContaBadge situacao={resumo.statusConta} />
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

function SituacaoContaBadge({ situacao }: { situacao: SituacaoConta }) {
  const map: Record<SituacaoConta, { cor: string; rotulo: string }> = {
    PENDENTE:     { cor: "text-amber-700",  rotulo: "Pendente" },
    ATIVA:        { cor: "text-brand-600",  rotulo: "Ativa" },
    INADIMPLENTE: { cor: "text-orange-600", rotulo: "Inadimplente" },
    SUSPENSA:     { cor: "text-paw-600",    rotulo: "Suspensa" },
  };
  const s = map[situacao];
  return <p className={`text-base font-semibold ${s.cor}`}>{s.rotulo}</p>;
}

function TabelaCobrancas({
  cobrancas, onPagar,
}: {
  cobrancas: Cobranca[];
  onPagar: (c: Cobranca) => void;
}) {
  if (cobrancas.length === 0) {
    return (
      <div className="card px-6 py-10 text-center text-sm text-ink-500">
        Você ainda não possui cobranças registradas.
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
            {cobrancas.map(c => (
              <tr key={c.id} className="hover:bg-ink-100/40">
                <Td>{formatarCompetencia(c.competencia)}</Td>
                <Td>{formatarReal(c.valorOriginal)}</Td>
                <Td>{c.descontoIndicacao > 0 ? formatarReal(c.descontoIndicacao) : "—"}</Td>
                <Td>{formatarReal(c.juros)}</Td>
                <Td className="font-semibold text-ink-900">{formatarReal(c.valorAtualizado)}</Td>
                <Td>{formatarData(c.vencimento)}</Td>
                <Td>{formatarData(c.dataPagamento)}</Td>
                <Td><StatusCobrancaBadge status={c.status} /></Td>
                <Td className="text-right">
                  {c.status !== "PAGA" && (
                    <button
                      onClick={() => onPagar(c)}
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

function StatusCobrancaBadge({ status }: { status: StatusCobranca }) {
  const map: Record<StatusCobranca, string> = {
    PAGA:      "text-brand-600",
    PENDENTE:  "text-amber-700",
    EM_ATRASO: "text-paw-600",
  };
  const rotulo: Record<StatusCobranca, string> = {
    PAGA: "Paga",
    PENDENTE: "Pendente",
    EM_ATRASO: "Em Atraso",
  };
  return <span className={`font-semibold ${map[status]}`}>{rotulo[status]}</span>;
}

function Anotacao() {
  return (
    <div className="rounded-xl border border-dashed border-ink-300 bg-white/60 p-4 text-xs leading-relaxed text-ink-500">
      Juros simples calculados automaticamente (0,033% ao dia). Conta <strong>Suspensa</strong> após 3
      cobranças consecutivas em atraso (F-07 RN 7). QR Code gerado dinamicamente com valor atualizado.
      Descontos de indicação aplicados automaticamente ao confirmar pagamento do indicado (F-04).
    </div>
  );
}
