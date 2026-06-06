package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

import java.time.LocalDateTime;

/** Resumo compacto de um protocolo (usado na listagem de protocolos ativos). */
public record StatusProtocoloDTO(String id, String atendimentoId, String pacienteId, String status,
                                 String nivelEscalonamentoAtual, LocalDateTime ativadoEm) {

    public static StatusProtocoloDTO de(ProtocoloInacessibilidade p) {
        return new StatusProtocoloDTO(
            p.getId().getValor(),
            p.getAtendimentoId().getValor(),
            p.getPacienteId().getValor(),
            p.getStatus().name(),
            p.getNivelEscalonamentoAtual() == null ? null : p.getNivelEscalonamentoAtual().name(),
            p.getAtivadoEm());
    }
}
