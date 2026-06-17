package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServicoNotificacaoLog implements IServicoNotificacao {

    private static final Logger log = LoggerFactory.getLogger(ServicoNotificacaoLog.class);

    @Override
    public void notificar(String destinatarioId, ConteudoNotificacao conteudo, NivelCriticidade criticidade) {
        log.info("[NOTIFICAÇÃO {} → {}] {} — {}",
            criticidade, destinatarioId, conteudo.getTitulo(), conteudo.getCorpo());
    }
}
