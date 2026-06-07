import { useState } from "react";
import { Modal } from "../compartilhados/Modal";
import { BannerErro } from "../compartilhados/Primitivos";

type Props = {
  aberto: boolean;
  executando: boolean;
  erro: string | null;
  onFechar: () => void;
  onConfirmar: (atendimentoId: string) => void;
};

/**
 * Ativação manual de um protocolo para um atendimento em curso. O backend recebe
 * apenas o id do atendimento; o motivo é um registro interno da recepção.
 */
export function ModalAtivacaoManual({ aberto, executando, erro, onFechar, onConfirmar }: Props) {
  const [atendimentoId, setAtendimentoId] = useState("");
  const [motivo, setMotivo] = useState("");

  const valido = atendimentoId.trim().length > 0;

  return (
    <Modal
      aberto={aberto}
      titulo="Ativar protocolo manualmente"
      onFechar={onFechar}
      rodape={
        <>
          <button onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">
            Cancelar
          </button>
          <button
            onClick={() => onConfirmar(atendimentoId)}
            disabled={!valido || executando}
            className="btn-primary w-auto bg-paw-500 hover:bg-paw-600"
          >
            {executando ? "Ativando…" : "Ativar protocolo"}
          </button>
        </>
      }
    >
      {erro && <BannerErro mensagem={erro} />}
      <label className="block">
        <span className="label">Atendimento em curso</span>
        <input
          className="input"
          placeholder="ID do atendimento"
          value={atendimentoId}
          onChange={(e) => setAtendimentoId(e.target.value)}
        />
      </label>
      <label className="block">
        <span className="label">Motivo da ativação manual (registro interno)</span>
        <textarea
          className="input min-h-[80px]"
          placeholder="Ex.: tutor saiu da sala e não respondeu ao chamado."
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
        />
      </label>
      <p className="text-xs text-ink-500">
        Confirme com cuidado: a ativação dispara o protocolo de contato para este atendimento.
      </p>
    </Modal>
  );
}
