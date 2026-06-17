package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import java.time.LocalDateTime;

public record RegistroNotificacaoProtocolo(
    String id,
    String protocoloId,
    String destinatarioId,
    String titulo,
    String corpo,
    String criticidade,
    LocalDateTime registradoEm
) {}
