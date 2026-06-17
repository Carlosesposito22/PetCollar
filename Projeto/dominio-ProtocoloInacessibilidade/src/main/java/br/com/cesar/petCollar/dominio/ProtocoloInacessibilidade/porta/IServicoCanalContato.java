package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;

public interface IServicoCanalContato {

    ResultadoContato contatar(CanalContato canal, String destinatarioId, ConteudoNotificacao conteudo);
}
