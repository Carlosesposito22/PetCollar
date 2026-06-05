import { useCallback, useEffect, useState } from "react";
import type { ExameDTO } from "./tipos";
import { ApiError } from "./agendamentoService";
import { useAgendamentoService } from "./useAgendamentoService";
import { useToast } from "./Toast";

/**
 * Lista (sempre renderizada — RN 8) os exames solicitados na consulta de origem e
 * permite confirmar/anexar laudo (RN 9). Exibe badge automático quando o exame já
 * está concluído por emissão de laudo (RN 12). Informa ao pai o nº de concluídos
 * para liberar/bloquear o avanço (RN 10).
 */
export function ListaExames({
  consultaId,
  onConcluidosChange,
}: {
  consultaId: string;
  onConcluidosChange: (concluidos: number, total: number) => void;
}) {
  const api = useAgendamentoService();
  const toast = useToast();
  const [exames, setExames] = useState<ExameDTO[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [exameLaudo, setExameLaudo] = useState<ExameDTO | null>(null);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const lista = await api.examesSolicitados(consultaId);
      setExames(lista);
      onConcluidosChange(lista.filter(e => e.status === "CONCLUIDO").length, lista.length);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [api, consultaId, onConcluidosChange]);

  useEffect(() => { void carregar(); }, [carregar]);

  async function confirmar(exame: ExameDTO) {
    try {
      await api.confirmarExame(consultaId, exame.exameId);
      toast.sucesso(`Exame “${exame.descricao}” marcado como concluído.`);
      await carregar();
    } catch (e) {
      toast.erro(e instanceof ApiError ? e.message : "Falha ao confirmar o exame.");
    }
  }

  const concluidos = exames.filter(e => e.status === "CONCLUIDO").length;

  if (carregando) {
    return (
      <div className="space-y-2">
        {[0, 1].map(i => <div key={i} className="h-16 animate-pulse rounded-xl bg-ink-100" />)}
      </div>
    );
  }

  if (erro) {
    return (
      <div role="alert" className="rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
        {erro}
      </div>
    );
  }

  return (
    <div>
      {exames.length === 0 ? (
        <div className="rounded-xl border border-dashed border-ink-300 px-4 py-8 text-center text-sm text-ink-500">
          Nenhum exame foi solicitado nesta consulta.
        </div>
      ) : (
        <>
          <p className="mb-3 text-sm font-medium text-ink-700">
            {concluidos} de {exames.length} exame(s) concluído(s)
          </p>
          <ul className="space-y-2">
            {exames.map(exame => (
              <ItemExame key={exame.exameId} exame={exame}
                         onConfirmar={() => confirmar(exame)}
                         onAnexarLaudo={() => setExameLaudo(exame)} />
            ))}
          </ul>
        </>
      )}

      {exameLaudo && (
        <UploadLaudo
          consultaId={consultaId}
          exame={exameLaudo}
          onFechar={() => setExameLaudo(null)}
          onEnviado={async () => { setExameLaudo(null); await carregar(); }}
        />
      )}
    </div>
  );
}

function ItemExame({
  exame, onConfirmar, onAnexarLaudo,
}: { exame: ExameDTO; onConfirmar: () => Promise<void>; onAnexarLaudo: () => void }) {
  const concluido = exame.status === "CONCLUIDO";
  const [confirmando, setConfirmando] = useState(false);

  async function confirmar() {
    setConfirmando(true);
    try { await onConfirmar(); } finally { setConfirmando(false); }
  }

  return (
    <li className={
      "flex flex-wrap items-center justify-between gap-3 rounded-xl border p-4 " +
      (concluido ? "border-emerald-200 bg-emerald-50/60" : "border-ink-300 bg-white")
    }>
      <div className="min-w-[180px]">
        <p className="font-semibold text-ink-900">{exame.descricao}</p>
        {concluido ? (
          <span className="mt-1 inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
            ✓ Concluído
          </span>
        ) : (
          <span className="mt-1 inline-flex items-center gap-1 rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
            Pendente
          </span>
        )}
      </div>
      {!concluido && (
        <div className="flex gap-2">
          <button type="button" onClick={confirmar} disabled={confirmando}
                  aria-busy={confirmando} className="btn-ghost ring-1 ring-ink-300">
            {confirmando ? "Concluindo…" : "Marcar como concluído"}
          </button>
          <button type="button" onClick={onAnexarLaudo} className="btn-ghost ring-1 ring-brand-200 text-brand-700">
            Anexar laudo
          </button>
        </div>
      )}
    </li>
  );
}

function UploadLaudo({
  consultaId, exame, onFechar, onEnviado,
}: { consultaId: string; exame: ExameDTO; onFechar: () => void; onEnviado: () => void }) {
  const api = useAgendamentoService();
  const toast = useToast();
  const [laudo, setLaudo] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await api.registrarLaudo(consultaId, exame.exameId, laudo.trim());
      toast.sucesso("Laudo anexado e exame concluído.");
      onEnviado();
    } catch (e2) {
      const msg = e2 instanceof ApiError ? e2.message : "Falha ao anexar o laudo.";
      setErro(msg);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="w-full max-w-md card p-6" onClick={e => e.stopPropagation()}>
        <h3 className="mb-1 text-lg font-bold text-ink-900">Anexar laudo</h3>
        <p className="mb-4 text-sm text-ink-500">Exame: <strong>{exame.descricao}</strong></p>
        {erro && (
          <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
            {erro}
          </div>
        )}
        <form onSubmit={enviar} className="grid gap-4">
          <div>
            <label className="label" htmlFor="laudo">Conteúdo / observações do laudo</label>
            <textarea id="laudo" required rows={4} className="input"
                      placeholder="Cole o resultado do exame ou descreva o laudo…"
                      value={laudo} onChange={e => setLaudo(e.target.value)} />
            <p className="mt-1 text-xs text-ink-500">
              Ao anexar o laudo, o exame é marcado como concluído automaticamente.
            </p>
          </div>
          <div className="mt-2 flex justify-end gap-2">
            <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
            <button type="submit" disabled={enviando || laudo.trim().length === 0}
                    aria-busy={enviando} className="btn-primary w-auto">
              {enviando ? "Enviando…" : "Anexar laudo"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
