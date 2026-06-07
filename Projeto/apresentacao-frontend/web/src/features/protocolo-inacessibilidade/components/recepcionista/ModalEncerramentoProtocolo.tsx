import { useState } from "react";
import type { StatusProtocoloDTO } from "../../tipos";
import { Modal } from "../compartilhados/Modal";
import { BannerErro } from "../compartilhados/Primitivos";

type Props = {
  alvo: StatusProtocoloDTO | null;
  executando: boolean;
  erro: string | null;
  onFechar: () => void;
  onConfirmar: (detalhes: string) => void;
};

/**
 * Encerramento manual de um protocolo. O backend fixa o motivo como
 * "intervenção manual"; a recepção informa os detalhes (obrigatórios).
 */
export function ModalEncerramentoProtocolo({
  alvo,
  executando,
  erro,
  onFechar,
  onConfirmar,
}: Props) {
  const [detalhes, setDetalhes] = useState("");
  const valido = detalhes.trim().length > 0;

  return (
    <Modal
      aberto={alvo != null}
      titulo="Encerrar protocolo manualmente"
      onFechar={onFechar}
      rodape={
        <>
          <button onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">
            Cancelar
          </button>
          <button
            onClick={() => onConfirmar(detalhes)}
            disabled={!valido || executando}
            className="btn-primary w-auto bg-paw-500 hover:bg-paw-600"
          >
            {executando ? "Encerrando…" : "Encerrar protocolo"}
          </button>
        </>
      }
    >
      {erro && <BannerErro mensagem={erro} />}
      {alvo && (
        <p className="text-sm text-ink-500">
          Protocolo do paciente <span className="font-medium text-ink-800">{alvo.pacienteId}</span>{" "}
          (atendimento {alvo.atendimentoId}).
        </p>
      )}
      <label className="block">
        <span className="label">Motivo</span>
        <input className="input" value="Intervenção manual" readOnly disabled />
      </label>
      <label className="block">
        <span className="label">Detalhes do encerramento</span>
        <textarea
          className="input min-h-[90px]"
          placeholder="Descreva por que o protocolo está sendo encerrado manualmente."
          value={detalhes}
          onChange={(e) => setDetalhes(e.target.value)}
        />
      </label>
    </Modal>
  );
}
