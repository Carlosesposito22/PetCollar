import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { QrCodeMock } from "../components/QrCodeMock";

type Tutor = {
  identificador: string;
  nome: string;
  email: string;
  status: string;
  codigoPix: string | null;
};

type LocationState = { tutor?: Tutor } | undefined;

export function PagamentoPendente() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const [tutor, setTutor] = useState<Tutor | null>((state as LocationState)?.tutor ?? null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!tutor) navigate("/login/tutor", { replace: true });
  }, [tutor, navigate]);

  if (!tutor) return null;

  async function confirmar() {
    setErro(null);
    setEnviando(true);
    try {
      const res = await fetch(
        `/api/tutores/${encodeURIComponent(tutor!.identificador)}/simular-pagamento`,
        { method: "POST" }
      );
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { mensagem?: string }));
        throw new Error(body?.mensagem ?? `Falha ao confirmar pagamento (HTTP ${res.status}).`);
      }
      const atualizado = (await res.json()) as Tutor;
      setTutor(atualizado);
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  function copiarCodigo() {
    if (!tutor!.codigoPix) return;
    navigator.clipboard.writeText(tutor!.codigoPix).catch(() => {});
  }

  const isAtivo = tutor.status === "ATIVA";

  return (
    <AuthLayout showBack>
      <div className="card p-8">
        <header className="mb-6 text-center">
          {isAtivo ? (
            <>
              <span className="chip bg-emerald-50 text-emerald-700 ring-emerald-100">
                Pagamento confirmado
              </span>
              <h2 className="mt-3 text-2xl font-bold text-ink-900">Tudo certo, {primeiroNome(tutor.nome)}!</h2>
              <p className="mt-1 text-sm text-ink-500">
                Sua conta foi ativada. Faça login para acessar o painel.
              </p>
            </>
          ) : (
            <>
              <span className="chip bg-amber-50 text-amber-700 ring-amber-100">
                Status da Conta: Pendente
              </span>
              <h2 className="mt-3 text-2xl font-bold text-ink-900">Pagamento Pendente</h2>
              <p className="mt-1 text-sm text-ink-500">
                Escaneie o QR Code ou copie o código abaixo para concluir o pagamento.
              </p>
            </>
          )}
        </header>

        {erro && (
          <div role="alert" className="mb-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            {erro}
          </div>
        )}

        {!isAtivo && (
          <>
            <div className="mb-5 flex items-center justify-center rounded-xl border border-ink-300 bg-white p-6">
              <QrCodeMock seed={tutor.identificador} />
            </div>

            <div className="mb-5">
              <div className="mb-1 text-xs font-medium uppercase tracking-wide text-ink-500">
                Código copia-e-cola:
              </div>
              <div className="flex items-stretch gap-2">
                <code className="block flex-1 truncate rounded-lg bg-ink-100 px-3 py-2 font-mono text-xs text-ink-700">
                  {tutor.codigoPix}
                </code>
                <button type="button" onClick={copiarCodigo} className="btn-ghost ring-1 ring-ink-300">
                  Copiar
                </button>
              </div>
            </div>

            <div className="mb-6 rounded-xl border-2 border-amber-300 bg-amber-50 p-4 text-center text-sm font-medium text-amber-900">
              ⚠️ Acesso será liberado somente após confirmação do pagamento
            </div>
          </>
        )}

        {isAtivo ? (
          <button type="button" onClick={() => navigate("/login/tutor")} className="btn-primary">
            Ir para Login
          </button>
        ) : (
          <button type="button" onClick={confirmar} disabled={enviando} className="btn-primary">
            {enviando ? "Confirmando…" : "Já paguei (simular confirmação)"}
          </button>
        )}
      </div>

      <p className="mt-4 text-center text-xs text-ink-500">
        Em produção, o status mudaria automaticamente via webhook do PSP após a quitação.
      </p>
    </AuthLayout>
  );
}

function primeiroNome(nome: string) {
  return nome?.split(" ")[0] ?? "tutor";
}

