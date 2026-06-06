package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import static org.junit.jupiter.api.Assertions.*;

public class PassosVisualizacaoStatus {

    private final ContextoCenario contexto;

    public PassosVisualizacaoStatus(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @E("o tutor consulta a situação do protocolo")
    public void consultaSituacao() {
        try {
            contexto.visao = contexto.statusService.montarVisao(contexto.atendimentoId);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Entao("a visão deve apresentar o status {string}")
    public void visaoComStatus(String status) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertNotNull(contexto.visao, "Nenhuma visão montada.");
        assertEquals(StatusProtocolo.valueOf(status), contexto.visao.getStatus());
    }

    @Entao("a visão deve conter {int} tentativa(s) no histórico")
    public void visaoComTentativas(int quantidade) {
        assertNotNull(contexto.visao, "Nenhuma visão montada.");
        assertEquals(quantidade, contexto.visao.getTentativas().size());
    }
}
