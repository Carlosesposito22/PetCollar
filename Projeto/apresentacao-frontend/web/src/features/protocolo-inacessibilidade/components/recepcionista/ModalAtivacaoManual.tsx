import { useState } from "react";
import { Modal } from "../compartilhados/Modal";
import { BannerErro, Skeleton } from "../compartilhados/Primitivos";
import { useAtendimentosEmAndamento } from "../../hooks/useAtendimentosEmAndamento";
import { tempoRelativo } from "../../tipos";

type Props = {
  aberto: boolean;
  executando: boolean;
  erro: string | null;
  onFechar: () => void;
  onConfirmar: (atendimentoId: string) => void;
};

/**
 * Ativação manual de um protocolo. Carrega os atendimentos em andamento ao
 * abrir e permite que a recepcionista selecione um antes de confirmar.
 */
export function ModalAtivacaoManual({ aberto, executando, erro, onFechar, onConfirmar }: Props) {
  const [selecionado, setSelecionado] = useState<string | null>(null);
  const [motivo, setMotivo] = useState("");
  const { dados, carregando, erro: erroLista, recarregar } = useAtendimentosEmAndamento(aberto);

  function fechar() {
    setSelecionado(null);
    setMotivo("");
    onFechar();
  }

  const valido = selecionado !== null;

  return (
    <Modal
      aberto={aberto}
      titulo="Ativar protocolo manualmente"
      onFechar={fechar}
      rodape={
        <>
          <button onClick={fechar} className="btn-ghost ring-1 ring-ink-300">
            Cancelar
          </button>
          <button
            onClick={() => selecionado && onConfirmar(selecionado)}
            disabled={!valido || executando}
            className="btn-primary w-auto bg-paw-500 hover:bg-paw-600"
          >
            {executando ? "Ativando…" : "Ativar protocolo"}
          </button>
        </>
      }
    >
      {erro && <BannerErro mensagem={erro} />}

      <div className="space-y-3">
        <span className="label">Atendimento em curso</span>

        {carregando && <Skeleton className="h-32" />}

        {erroLista && (
          <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            <span>{erroLista}</span>
            <button onClick={recarregar} className="ml-2 underline hover:no-underline">
              Tentar novamente
            </button>
          </div>
        )}

        {!carregando && !erroLista && dados.length === 0 && (
          <p className="rounded-lg border border-ink-200 bg-ink-50 px-4 py-3 text-sm text-ink-500">
            Nenhum atendimento em andamento no momento.
          </p>
        )}

        {!carregando && dados.length > 0 && (
          <ul className="max-h-56 overflow-y-auto divide-y divide-ink-100 rounded-xl border border-ink-200">
            {dados.map((a) => {
              const ativo = selecionado === a.atendimentoId;
              return (
                <li key={a.atendimentoId}>
                  <button
                    type="button"
                    onClick={() => setSelecionado(a.atendimentoId)}
                    className={[
                      "w-full px-4 py-3 text-left transition-colors",
                      ativo
                        ? "bg-brand-50 ring-inset ring-2 ring-brand-400"
                        : "hover:bg-ink-50",
                    ].join(" ")}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium text-ink-900">
                          {a.nomePaciente ?? (
                            <>
                              Paciente{" "}
                              <span className="font-mono text-xs text-ink-500">
                                {a.pacienteId}
                              </span>
                            </>
                          )}
                        </p>
                        <p className="mt-0.5 text-xs text-ink-500">
                          Tutor{" "}
                          <span className="font-mono">{a.tutorPrincipalId}</span>
                          {" · "}última interação {tempoRelativo(a.ultimaInteracaoTutorEm)}
                        </p>
                      </div>
                      {ativo && (
                        <span className="shrink-0 text-brand-600" aria-hidden>
                          ✓
                        </span>
                      )}
                    </div>
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      <label className="block">
        <span className="label">Motivo da ativação manual (registro interno)</span>
        <textarea
          className="input min-h-[72px]"
          placeholder="Ex.: tutor saiu da sala e não respondeu ao chamado."
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
        />
      </label>

      <p className="text-xs text-ink-500">
        Confirme com cuidado: a ativação dispara o protocolo de contato para o atendimento
        selecionado.
      </p>
    </Modal>
  );
}
