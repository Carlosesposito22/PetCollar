package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

/**
 * Estratégia de execução de uma tentativa de contato em um canal específico. Cada
 * canal real (telefone, SMS, e-mail, WhatsApp) tem sua implementação; o
 * {@link ServicoCanalContatoDispatcher} roteia a tentativa para a estratégia
 * adequada (Strategy + Adapter).
 */
public interface EstrategiaCanalContato {

    CanalContato canal();

    ResultadoContato contatar(String destinatarioId, ConteudoNotificacao conteudo);
}
