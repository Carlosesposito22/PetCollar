package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import java.util.List;

/**
 * Porta de saída para consultar o histórico auditável de notificações de um protocolo
 * (RN 16). Separada de {@link IServicoNotificacao} para seguir o princípio de
 * segregação de interfaces: escrita e leitura têm ciclos de vida distintos.
 */
public interface IConsultaNotificacaoProtocolo {

    /** Retorna as notificações do protocolo ordenadas da mais recente à mais antiga. */
    List<RegistroNotificacaoProtocolo> listarPorProtocolo(String protocoloId);
}
