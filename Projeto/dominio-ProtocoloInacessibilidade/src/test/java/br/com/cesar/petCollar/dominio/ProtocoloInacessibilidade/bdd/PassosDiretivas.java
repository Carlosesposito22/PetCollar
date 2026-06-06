package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PassosDiretivas {

    private final ContextoCenario contexto;

    public PassosDiretivas(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("uma diretiva que autoriza a conduta {string}")
    public void diretivaAutoriza(String conduta) {
        when(contexto.diretivas.verificarAutorizacao(
            eq(contexto.pacienteId), eq(TipoConduta.valueOf(conduta)))).thenReturn(true);
    }

    @Dado("uma diretiva que não autoriza a conduta {string}")
    public void diretivaNaoAutoriza(String conduta) {
        when(contexto.diretivas.verificarAutorizacao(
            eq(contexto.pacienteId), eq(TipoConduta.valueOf(conduta)))).thenReturn(false);
    }

    @Quando("o sistema consulta a autorização da conduta {string}")
    public void consultaAutorizacao(String conduta) {
        contexto.autorizacaoConduta =
            contexto.diretivaService.podeExecutarConduta(contexto.pacienteId, TipoConduta.valueOf(conduta));
    }

    @Entao("a conduta deve ser autorizada")
    public void condutaAutorizada() {
        assertNotNull(contexto.autorizacaoConduta);
        assertTrue(contexto.autorizacaoConduta);
    }

    @Entao("a conduta deve ser bloqueada")
    public void condutaBloqueada() {
        assertNotNull(contexto.autorizacaoConduta);
        assertFalse(contexto.autorizacaoConduta);
    }
}
