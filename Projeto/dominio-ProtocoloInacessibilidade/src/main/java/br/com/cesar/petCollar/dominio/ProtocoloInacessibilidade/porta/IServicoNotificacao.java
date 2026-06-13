package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;

/**
 * Porta de saída para o contexto Notificacao. Permite que os serviços do protocolo
 * avisem tutor, responsáveis secundários e níveis de escalonamento (RN 9, 11, 12,
 * 13, 14) sem acoplamento direto a um canal real.
 *
 * <p><b>RN 16 (auditabilidade):</b> espera-se que a implementação concreta deste
 * contrato <b>persista cada notificação</b> de forma auditável no contexto
 * Notificacao (destinatário, canal, conteúdo, status de entrega e instante).
 */
public interface IServicoNotificacao {

    void notificar(String destinatarioId, ConteudoNotificacao conteudo, NivelCriticidade criticidade);

    /**
     * Notifica associando ao protocolo em execução (RN 16 — auditabilidade). A
     * implementação padrão delega ao método sem contexto; sobrescreva para persistir.
     */
    default void notificar(String destinatarioId, ConteudoNotificacao conteudo,
                           NivelCriticidade criticidade, String protocoloId) {
        notificar(destinatarioId, conteudo, criticidade);
    }
}
