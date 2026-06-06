package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;

/**
 * Porta de saída para executar uma tentativa de contato real em um canal
 * específico (telefone, SMS, e-mail, WhatsApp — RN 2). A implementação concreta
 * de cada canal vive na infraestrutura/apresentação; o domínio só conhece este
 * contrato e o {@link ResultadoContato} devolvido.
 */
public interface IServicoCanalContato {

    ResultadoContato contatar(CanalContato canal, String destinatarioId, ConteudoNotificacao conteudo);
}
