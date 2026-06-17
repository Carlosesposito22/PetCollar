package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoCanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ServicoCanalContatoDispatcher implements IServicoCanalContato {

    private final Map<CanalContato, EstrategiaCanalContato> estrategias = new EnumMap<>(CanalContato.class);

    public ServicoCanalContatoDispatcher(List<EstrategiaCanalContato> estrategias) {
        for (EstrategiaCanalContato estrategia : estrategias) {
            this.estrategias.put(estrategia.canal(), estrategia);
        }
    }

    @Override
    public ResultadoContato contatar(CanalContato canal, String destinatarioId, ConteudoNotificacao conteudo) {
        EstrategiaCanalContato estrategia = estrategias.get(canal);
        if (estrategia == null)
            return ResultadoContato.falhaTecnica("Nenhuma estratégia registrada para o canal " + canal + ".");
        return estrategia.contatar(destinatarioId, conteudo);
    }
}
