package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.EventoEscalonamento;

import java.time.LocalDateTime;

public record EventoEscalonamentoDTO(String id, String nivel, String motivo,
                                     String responsavelAcionadoId, LocalDateTime ocorridoEm) {

    public static EventoEscalonamentoDTO de(EventoEscalonamento e) {
        return new EventoEscalonamentoDTO(
            e.getId().getValor(),
            e.getNivel().name(),
            e.getMotivo(),
            e.getResponsavelAcionadoId(),
            e.getOcorridoEm());
    }
}
