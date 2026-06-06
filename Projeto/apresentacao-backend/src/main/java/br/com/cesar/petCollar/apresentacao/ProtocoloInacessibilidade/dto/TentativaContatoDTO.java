package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;

import java.time.LocalDateTime;

public record TentativaContatoDTO(String id, String destinatarioId, String tipoDestinatario,
                                  String canal, String status, LocalDateTime executadaEm,
                                  String mensagemRetorno) {

    public static TentativaContatoDTO de(TentativaContato t) {
        return new TentativaContatoDTO(
            t.getId().getValor(),
            t.getDestinatarioId(),
            t.getTipoDestinatario().name(),
            t.getCanal().name(),
            t.getStatus().name(),
            t.getExecutadaEm(),
            t.getMensagemRetorno());
    }
}
