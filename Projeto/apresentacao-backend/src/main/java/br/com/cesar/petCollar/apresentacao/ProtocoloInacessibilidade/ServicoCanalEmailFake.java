package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fake do canal EMAIL: registra o envio e devolve "sem resposta".
 */
@Component
public class ServicoCanalEmailFake implements EstrategiaCanalContato {

    private static final Logger log = LoggerFactory.getLogger(ServicoCanalEmailFake.class);

    @Override
    public CanalContato canal() {
        return CanalContato.EMAIL;
    }

    @Override
    public ResultadoContato contatar(String destinatarioId, ConteudoNotificacao conteudo) {
        log.info("[CANAL EMAIL → {}] e-mail enviado: {}", destinatarioId, conteudo.getTitulo());
        return ResultadoContato.semResposta("E-mail entregue, sem resposta.");
    }
}
