import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { QrCodeMock } from "../../components/QrCodeMock";
import { formatarCompetencia, formatarData, formatarReal } from "../../utils/formato";

type DetalhePagamento = {
  id: string;
  competencia: string;
  vencimento: string;
  valorOriginal: number;
  descontoIndicacao: number;
  juros: number;
  diasAtraso: number;
  taxaDiaria: number;
  valorAtualizado: number;
  status: "PENDENTE" | "EM_ATRASO" | "PAGA";
  codigoPix: string;
};

export function TutorFinanceiroPagamento() {
  const { id } = useParams<{ id: string }>();
  const { apiFetch } = useAuth();
  const navigate = useNavigate();
  const [detalhe, setDetalhe] = useState<DetalhePagamento | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [pagando, setPagando] = useState(false);
  const [confirmado, setConfirmado] = useState(false);

  const carregar = useCallback(async () => {
    if (!id) return;
    setErro(null);
    setCarregando(true);
    try {
      const res = await apiFetch(`/api/tutor/financeiro/cobrancas/${id}`);
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha (HTTP ${res.status}).`);
      }
      setDetalhe(await res.json());
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [apiFetch, id]);

  useEffect(() => { void carregar(); }, [carregar]);

  async function confirmarPagamento() {
    if (!id) return;
    setErro(null);
    setPagando(true);
    try {
      const res = await apiFetch(`/api/tutor/financeiro/cobrancas/${id}/pagar`, { method: "POST" });
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha ao confirmar (HTTP ${res.status}).`);
      }
      setConfirmado(true);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setPagando(false);
    }
  }

  function copiarCodigo() {
    if (!detalhe?.codigoPix) return;
    navigator.clipboard.writeText(detalhe.codigoPix).catch(() => {});
  }

  return (
    <div>
      <button
        onClick={() => navigate("/app/financeiro")}
        className="mb-4 inline-flex items-center gap-1 text-sm font-medium text-ink-500 hover:text-brand-600"
      >
        ← Voltar
      </button>
      <h1 className="mb-6 text-2xl font-bold text-ink-900">Área Financeira</h1>

      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
          {erro}
        </div>
      )}

      {carregando && <p className="text-sm text-ink-500">Carregando…</p>}

      {detalhe && !carregando && (
        <section className="card overflow-hidden">
          <header className="border-b border-ink-300/40 bg-ink-100/40 p-6">
            <h2 className="text-lg font-bold text-ink-900">
              Pagamento — Competência {formatarCompetencia(detalhe.competencia)}
            </h2>
            <p className="mt-1 text-sm text-ink-500">
              Vencimento original em {formatarData(detalhe.vencimento)}.
            </p>
          </header>

          {confirmado ? (
            <ConfirmacaoBox onVoltar={() => navigate("/app/financeiro")} />
          ) : (
            <div className="grid gap-6 p-6 lg:grid-cols-2">
              <Breakdown detalhe={detalhe} />
              <PagamentoPix detalhe={detalhe} onCopiar={copiarCodigo} onConfirmar={confirmarPagamento} pagando={pagando} />
            </div>
          )}
        </section>
      )}
    </div>
  );
}

function Breakdown({ detalhe }: { detalhe: DetalhePagamento }) {
  return (
    <div className="card bg-white p-5 ring-1 ring-ink-300/60">
      <Linha rotulo="Valor Principal" valor={formatarReal(detalhe.valorOriginal)} />
      {detalhe.descontoIndicacao > 0 && (
        <Linha rotulo="Desconto Indicação" valor={`- ${formatarReal(detalhe.descontoIndicacao)}`} />
      )}
      <Linha
        rotulo={
          detalhe.diasAtraso > 0
            ? `Juros (${(detalhe.taxaDiaria * 100).toFixed(3)}% ao dia × ${detalhe.diasAtraso} dias)`
            : "Juros"
        }
        valor={formatarReal(detalhe.juros)}
      />
      <div className="mt-3 flex items-baseline justify-between border-t border-ink-300/40 pt-3">
        <span className="text-sm font-bold text-ink-900">Total Atualizado</span>
        <span className="text-2xl font-bold text-brand-600">{formatarReal(detalhe.valorAtualizado)}</span>
      </div>
    </div>
  );
}

function Linha({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div className="flex items-baseline justify-between py-1.5">
      <span className="text-sm text-ink-700">{rotulo}:</span>
      <span className="text-sm font-medium text-ink-900">{valor}</span>
    </div>
  );
}

function PagamentoPix({
  detalhe, onCopiar, onConfirmar, pagando,
}: {
  detalhe: DetalhePagamento;
  onCopiar: () => void;
  onConfirmar: () => void;
  pagando: boolean;
}) {
  return (
    <div className="card flex flex-col bg-white p-5 ring-1 ring-ink-300/60">
      <div className="mb-3 flex items-center justify-center rounded-xl border border-ink-300 bg-white p-4">
        <QrCodeMock seed={detalhe.id} size={180} />
      </div>
      <p className="text-center text-xs font-medium uppercase tracking-wide text-ink-500">
        QR Code de Pagamento
      </p>

      <div className="mt-4">
        <p className="mb-1 text-xs font-medium uppercase tracking-wide text-ink-500">
          Código copia-e-cola:
        </p>
        <div className="flex items-stretch gap-2">
          <code className="block flex-1 truncate rounded-lg bg-ink-100 px-3 py-2 font-mono text-xs text-ink-700">
            {detalhe.codigoPix}
          </code>
          <button type="button" onClick={onCopiar} className="btn-ghost ring-1 ring-ink-300">
            Copiar
          </button>
        </div>
      </div>

      <button
        type="button"
        onClick={onConfirmar}
        disabled={pagando}
        className="btn-primary mt-5 w-full"
      >
        {pagando ? "Confirmando…" : "Já paguei (simular confirmação)"}
      </button>

      <p className="mt-3 text-center text-[11px] text-ink-500">
        Em produção, o status mudaria automaticamente via webhook do PSP após a quitação.
      </p>
    </div>
  );
}

function ConfirmacaoBox({ onVoltar }: { onVoltar: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center px-6 py-10 text-center">
      <div className="mb-3 text-4xl">✅</div>
      <h3 className="text-xl font-bold text-ink-900">Pagamento confirmado</h3>
      <p className="mt-1 max-w-md text-sm text-ink-500">
        A mensalidade foi quitada e seu status da conta foi atualizado.
      </p>
      <button onClick={onVoltar} className="btn-primary mt-5 w-auto">
        Voltar para o financeiro
      </button>
    </div>
  );
}
