package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class PassosComuns {

    private final ContextoCenario contexto;

    public PassosComuns(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("uma configuração de protocolo vigente")
    public void configuracaoVigente() {
        contexto.configuracaoPadrao();
    }

    @E("um protocolo ativado para o atendimento")
    public void protocoloAtivado() {
        contexto.protocoloAtivado();
    }

    @E("um protocolo pronto para escalonamento")
    public void protocoloProntoParaEscalonamento() {
        contexto.protocoloProntoParaEscalonar();
    }

    @Entao("a operação deve ser recusada por regra de negócio")
    public void recusadaPorRegraDeNegocio() {
        assertNotNull(contexto.excecao, "Esperava-se uma exceção de regra de negócio.");
        assertInstanceOf(IllegalStateException.class, contexto.excecao);
    }

    @Entao("a operação deve ser recusada com erro de argumento")
    public void recusadaComErroDeArgumento() {
        assertNotNull(contexto.excecao, "Esperava-se uma exceção de argumento inválido.");
        assertInstanceOf(IllegalArgumentException.class, contexto.excecao);
    }

    @Entao("o protocolo deve ficar com status {string}")
    public void protocoloComStatus(String status) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertNotNull(contexto.protocolo, "Nenhum protocolo no contexto.");
        assertEquals(StatusProtocolo.valueOf(status), contexto.protocolo.getStatus());
    }

    @Entao("o tutor deve ser notificado")
    public void tutorNotificado() {
        verify(contexto.notificacao, atLeastOnce())
            .notificar(eq(contexto.tutorId.getValor()), any(), any());
    }
}
