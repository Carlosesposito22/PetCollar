package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import java.time.LocalDateTime;

/**
 * Projeção (read-model) de uma notificação auditável enviada durante a execução do
 * protocolo de inacessibilidade (RN 16). Permite que a camada de apresentação
 * consulte o histórico sem acoplamento direto à infraestrutura.
 */
public record RegistroNotificacaoProtocolo(
    String id,
    String protocoloId,
    String destinatarioId,
    String titulo,
    String corpo,
    String criticidade,
    LocalDateTime registradoEm
) {}
