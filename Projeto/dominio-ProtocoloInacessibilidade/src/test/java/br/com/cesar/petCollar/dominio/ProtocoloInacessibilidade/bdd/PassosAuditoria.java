package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.Entao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PassosAuditoria {

    private final ContextoCenario contexto;

    public PassosAuditoria(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Entao("o serviço de notificação deve ter sido acionado {int} vezes")
    public void notificacaoAcionada(int vezes) {

        verify(contexto.notificacao, times(vezes)).notificar(any(), any(), any(), any());
    }
}
