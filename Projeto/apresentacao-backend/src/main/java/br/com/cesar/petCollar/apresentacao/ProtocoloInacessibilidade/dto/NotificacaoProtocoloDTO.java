package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.RegistroNotificacaoProtocolo;

import java.time.LocalDateTime;

/** Registro auditável de notificação enviada durante a execução do protocolo (RN 16). */
public record NotificacaoProtocoloDTO(String id, String destinatarioId, String titulo,
                                      String corpo, String criticidade,
                                      LocalDateTime registradoEm) {

    public static NotificacaoProtocoloDTO de(RegistroNotificacaoProtocolo r) {
        return new NotificacaoProtocoloDTO(
            r.id(), r.destinatarioId(), r.titulo(), r.corpo(),
            r.criticidade(), r.registradoEm());
    }
}
