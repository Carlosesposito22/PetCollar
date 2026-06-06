package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.VisaoProtocolo;

import java.time.LocalDateTime;
import java.util.List;

/** Visão consolidada do protocolo para o tutor (RN 15). */
public record VisaoProtocoloDTO(String protocoloId, String atendimentoId, String status,
                                String nivelEscalonamentoAtual, LocalDateTime ativadoEm,
                                LocalDateTime encerradoEm, List<TentativaContatoDTO> tentativas,
                                List<EventoEscalonamentoDTO> eventosEscalonamento) {

    public static VisaoProtocoloDTO de(VisaoProtocolo v) {
        return new VisaoProtocoloDTO(
            v.getProtocoloId().getValor(),
            v.getAtendimentoId().getValor(),
            v.getStatus().name(),
            v.getNivelEscalonamentoAtual() == null ? null : v.getNivelEscalonamentoAtual().name(),
            v.getAtivadoEm(),
            v.getEncerradoEm(),
            v.getTentativas().stream().map(TentativaContatoDTO::de).toList(),
            v.getEventosEscalonamento().stream().map(EventoEscalonamentoDTO::de).toList());
    }
}
