package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída (anticorrupção) para consultar atendimentos clínicos em andamento
 * — usada pela ativação por timeout (RN 1) e pelo monitoramento do scheduler. É um
 * contrato de <b>leitura</b>: o protocolo nunca interfere no atendimento (RN 8).
 */
public interface IConsultaAtendimento {

    Optional<ResumoAtendimento> buscarResumo(AtendimentoId atendimentoId);

    /** Atendimentos atualmente em andamento, varridos periodicamente pela RN 1. */
    List<ResumoAtendimento> listarEmAndamento();
}
