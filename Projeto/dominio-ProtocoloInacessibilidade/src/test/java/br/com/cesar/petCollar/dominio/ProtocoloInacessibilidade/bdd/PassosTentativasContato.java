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
        // Comportamento padrão do mock (SEM_RESPOSTA) já cobre este passo.
    }

    @E("o tutor responde no canal {string}")
    public void tutorRespondeNoCanal(String canal) {
        when(contexto.canalContato.contatar(eq(CanalContato.valueOf(canal)), any(), any()))
            .thenReturn(ResultadoContato.sucesso("Tutor atendeu."));
    }

    @E("todos os canais de contato com o tutor foram esgotados")
    public void canaisEsgotados() {
        int totalTentativas = contexto.configuracao.getCanaisHabilitados().size()
            * contexto.configuracao.getQuantidadeMaximaTentativasPorCanal();
        for (int i = 0; i < totalTentativas; i++) {
            contexto.execucaoService.executarProximoCanal(contexto.protocolo.getId());
        }
    }

    @Quando("o sistema executa a próxima tentativa de contato")
    public void executaProximaTentativa() {
        try {
            contexto.ultimaTentativa =
                contexto.execucaoService.executarProximoCanal(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema executa {int} tentativas de contato seguidas")
    public void executaVariasTentativas(int quantidade) {
        try {
            for (int i = 0; i < quantidade; i++) {
                contexto.ultimaTentativa =
                    contexto.execucaoService.executarProximoCanal(contexto.protocolo.getId());
            }
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema tenta executar mais uma tentativa de contato")
    public void tentaMaisUmaTentativa() {
        executaProximaTentativa();
    }

    @Entao("uma tentativa deve ser registrada com status {string}")
    public void tentativaRegistradaComStatus(String status) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertNotNull(contexto.ultimaTentativa, "Nenhuma tentativa registrada.");
        assertEquals(StatusTentativa.valueOf(status), contexto.ultimaTentativa.getStatus());
        assertTrue(contexto.protocolo.getTentativas().contains(contexto.ultimaTentativa),
            "A tentativa deveria estar registrada no agregado.");
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
            eq(contexto.tutorId.getValor()), any(), eq(NivelCriticidade.valueOf(criticidade)));
    }
}
