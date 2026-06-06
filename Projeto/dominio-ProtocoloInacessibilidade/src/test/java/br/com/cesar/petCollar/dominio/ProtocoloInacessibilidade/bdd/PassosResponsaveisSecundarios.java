package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundarioId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PassosResponsaveisSecundarios {

    private final ContextoCenario contexto;

    public PassosResponsaveisSecundarios(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @E("{int} responsáveis secundários cadastrados que não respondem")
    public void responsaveisQueNaoRespondem(int quantidade) {
        List<ResponsavelSecundario> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            lista.add(new ResponsavelSecundario(
                ResponsavelSecundarioId.gerar(), "Responsável " + (i + 1), i + 1,
                List.of(CanalContato.TELEFONE)));
        }
        when(contexto.responsaveis.listarPorPaciente(contexto.pacienteId)).thenReturn(lista);
    }

    @E("um responsável secundário que responde no canal {string}")
    public void responsavelQueResponde(String canal) {
        ResponsavelSecundario responsavel = new ResponsavelSecundario(
            ResponsavelSecundarioId.gerar(), "Responsável que responde", 1,
            List.of(CanalContato.valueOf(canal)));
        when(contexto.responsaveis.listarPorPaciente(contexto.pacienteId))
            .thenReturn(List.of(responsavel));
        when(contexto.canalContato.contatar(eq(CanalContato.valueOf(canal)), any(), any()))
            .thenReturn(ResultadoContato.sucesso("Responsável atendeu."));
    }

    @Quando("o sistema aciona todos os responsáveis secundários")
    public void acionaTodos() {
        try {
            contexto.tentativasResultantes =
                contexto.acionamentoService.acionarTodos(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema tenta iniciar o escalonamento")
    public void tentaIniciarEscalonamento() {
        try {
            contexto.escalonamentoService.iniciarEscalonamento(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Entao("todos os responsáveis secundários devem ter sido acionados")
    public void todosAcionados() {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertTrue(contexto.protocolo.todosResponsaveisSecundariosAcionados());
    }

    @Entao("o responsável secundário deve ser notificado com criticidade {string}")
    public void responsavelNotificadoComCriticidade(String criticidade) {
        verify(contexto.notificacao, atLeastOnce())
            .notificar(any(), any(), eq(NivelCriticidade.valueOf(criticidade)));
    }
}
