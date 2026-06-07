package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class PassosContinuidade {

    private final ContextoCenario contexto;

    public PassosContinuidade(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @E("um atendimento clínico em andamento")
    public void atendimentoClinicoEmAndamento() {
        ResumoAtendimento resumo = new ResumoAtendimento(
            contexto.atendimentoId, contexto.pacienteId, contexto.tutorId,
            LocalDateTime.now().minusMinutes(30), true);
        when(contexto.atendimentos.buscarResumo(contexto.atendimentoId))
            .thenReturn(java.util.Optional.of(resumo));
        when(contexto.atendimentos.listarEmAndamento()).thenReturn(List.of(resumo));
    }

    @Quando("o sistema ativa o protocolo e executa a etapa de contato com o tutor")
    public void ativaEExecutaEtapaContato() {
        try {
            contexto.ativacaoService.verificarEAtivar(contexto.atendimentoId)
                .ifPresent(p -> contexto.protocolo = p);
            contexto.etapaTutor.executar(contexto.protocolo.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Entao("o atendimento deve permanecer em andamento")
    public void atendimentoPermaneceEmAndamento() {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertTrue(contexto.atendimentos.buscarResumo(contexto.atendimentoId)
            .orElseThrow().isEmAndamento(),
            "O atendimento clínico não pode ser alterado pelo protocolo (RN 8).");
    }
}
