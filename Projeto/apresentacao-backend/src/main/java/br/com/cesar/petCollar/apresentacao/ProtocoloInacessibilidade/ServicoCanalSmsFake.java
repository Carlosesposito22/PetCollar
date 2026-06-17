package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServicoCanalSmsFake implements EstrategiaCanalContato {

    private static final Logger log = LoggerFactory.getLogger(ServicoCanalSmsFake.class);

    @Override
    public CanalContato canal() {
        return CanalContato.SMS;
    }

    @Override
    public ResultadoContato contatar(String destinatarioId, ConteudoNotificacao conteudo) {
        log.info("[CANAL SMS → {}] mensagem enviada: {}", destinatarioId, conteudo.getTitulo());
        return ResultadoContato.semResposta("SMS entregue, sem resposta.");
    }
}
