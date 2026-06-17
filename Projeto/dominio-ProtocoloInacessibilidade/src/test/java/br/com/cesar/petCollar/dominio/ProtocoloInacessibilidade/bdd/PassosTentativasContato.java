package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusTentativa;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PassosTentativasContato {

    private final ContextoCenario contexto;

    public PassosTentativasContato(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @E("o tutor não responde em nenhum canal")
    public void tutorNaoResponde() {

    }

    @E("o tutor responde no canal {string}")
    public void tutorRespondeNoCanal(String canal) {
        when(contexto.canalContato.contatar(eq(CanalContato.valueOf(canal)), any(), any()))
            .thenReturn(ResultadoContato.sucesso("Tutor atendeu."));
    }

    @Quando("o sistema executa a etapa de contato com o tutor")
    public void executaEtapaContatoTutor() {
        try {
            contexto.resultadoEtapa = contexto.etapaTutor.executar(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Entao("o protocolo deve conter {int} tentativa(s) de contato")
    public void protocoloContemTentativas(int quantidade) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertEquals(quantidade, contexto.protocolo.getTentativas().size());
    }

    @Entao("todas as tentativas devem ter status {string}")
    public void todasTentativasComStatus(String status) {
        StatusTentativa esperado = StatusTentativa.valueOf(status);
        assertFalse(contexto.protocolo.getTentativas().isEmpty(), "Nenhuma tentativa registrada.");
        assertTrue(contexto.protocolo.getTentativas().stream().allMatch(t -> t.getStatus() == esperado),
            "Todas as tentativas deveriam ter status " + esperado + ".");
    }

    @Entao("os canais utilizados nas tentativas devem ser {string}")
    public void canaisUtilizados(String canaisCsv) {
        String utilizados = contexto.protocolo.getTentativas().stream()
            .map(TentativaContato::getCanal)
            .map(Enum::name)
            .collect(Collectors.joining(","));
        assertEquals(canaisCsv, utilizados);
    }

    @Entao("a última notificação ao tutor deve ter criticidade {string}")
    public void notificacaoComCriticidade(String criticidade) {
        verify(contexto.notificacao, atLeastOnce()).notificar(
            eq(contexto.tutorId.getValor()), any(), eq(NivelCriticidade.valueOf(criticidade)), any());
    }
}
