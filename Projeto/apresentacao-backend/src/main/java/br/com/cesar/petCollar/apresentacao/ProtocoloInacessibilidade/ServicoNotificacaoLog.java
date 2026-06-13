package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Implementação alternativa (desativada — não é bean) que apenas loga notificações.
 * O bean primário é {@link ServicoNotificacaoEmMemoria}, que loga E persiste em memória
 * satisfazendo a auditabilidade da RN 16.
 */
public class ServicoNotificacaoLog implements IServicoNotificacao {

    private static final Logger log = LoggerFactory.getLogger(ServicoNotificacaoLog.class);

    @Override
    public void notificar(String destinatarioId, ConteudoNotificacao conteudo, NivelCriticidade criticidade) {
        log.info("[NOTIFICAÇÃO {} → {}] {} — {}",
            criticidade, destinatarioId, conteudo.getTitulo(), conteudo.getCorpo());
    }
}
