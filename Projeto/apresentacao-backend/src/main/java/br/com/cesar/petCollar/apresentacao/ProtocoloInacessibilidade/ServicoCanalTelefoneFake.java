package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fake do canal TELEFONE: registra a chamada e devolve "sem resposta" (sem integrar
 * com gateway real). Determinístico, para desbloquear a entrega e os testes.
 */
@Component
public class ServicoCanalTelefoneFake implements EstrategiaCanalContato {

    private static final Logger log = LoggerFactory.getLogger(ServicoCanalTelefoneFake.class);

    @Override
    public CanalContato canal() {
        return CanalContato.TELEFONE;
    }

    @Override
    public ResultadoContato contatar(String destinatarioId, ConteudoNotificacao conteudo) {
        log.info("[CANAL TELEFONE → {}] tentativa de ligação: {}", destinatarioId, conteudo.getTitulo());
        return ResultadoContato.semResposta("Ligação não atendida.");
    }
}
