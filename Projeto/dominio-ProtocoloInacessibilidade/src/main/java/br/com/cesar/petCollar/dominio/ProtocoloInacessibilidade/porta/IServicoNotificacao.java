package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;

public interface IServicoNotificacao {

    void notificar(String destinatarioId, ConteudoNotificacao conteudo, NivelCriticidade criticidade);

    default void notificar(String destinatarioId, ConteudoNotificacao conteudo,
                           NivelCriticidade criticidade, String protocoloId) {
        notificar(destinatarioId, conteudo, criticidade);
    }
}
