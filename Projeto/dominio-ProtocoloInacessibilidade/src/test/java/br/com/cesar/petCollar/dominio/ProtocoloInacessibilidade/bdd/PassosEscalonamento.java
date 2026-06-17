package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import static org.junit.jupiter.api.Assertions.*;

public class PassosEscalonamento {

    private final ContextoCenario contexto;

    public PassosEscalonamento(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Quando("o sistema inicia o escalonamento")
    public void iniciaEscalonamento() {
        try {
            contexto.etapaEscalonamento.executar(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema avança o nível de escalonamento")
    public void avancaNivel() {
        try {
            contexto.etapaEscalonamento.executar(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema avança o escalonamento {int} vezes")
    public void avancaVariasVezes(int vezes) {
        try {
            for (int i = 0; i < vezes; i++) {
                contexto.etapaEscalonamento.executar(contexto.protocolo.getId());
            }
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema executa a próxima etapa do protocolo")
    public void executaProximaEtapa() {
        try {
            contexto.resultadoEtapa = contexto.orquestrador.executarProximaEtapa(contexto.protocolo);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Entao("o nível de escalonamento atual deve ser {string}")
    public void nivelAtual(String nivel) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertEquals(NivelEscalonamento.valueOf(nivel), contexto.protocolo.getNivelEscalonamentoAtual());
    }

    @Entao("deve haver {int} evento(s) de escalonamento registrado(s)")
    public void quantidadeDeEventos(int quantidade) {
        assertEquals(quantidade, contexto.protocolo.getEventosEscalonamento().size());
    }
}
