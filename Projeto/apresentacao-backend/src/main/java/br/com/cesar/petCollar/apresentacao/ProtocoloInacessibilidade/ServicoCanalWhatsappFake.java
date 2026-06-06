package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fake do canal WHATSAPP: registra o envio e devolve "sem resposta".
 */
@Component
public class ServicoCanalWhatsappFake implements EstrategiaCanalContato {

    private static final Logger log = LoggerFactory.getLogger(ServicoCanalWhatsappFake.class);

    @Override
    public CanalContato canal() {
        return CanalContato.WHATSAPP;
    }

    @Override
    public ResultadoContato contatar(String destinatarioId, ConteudoNotificacao conteudo) {
        log.info("[CANAL WHATSAPP → {}] mensagem enviada: {}", destinatarioId, conteudo.getTitulo());
        return ResultadoContato.semResposta("Mensagem entregue, sem resposta.");
    }
}
