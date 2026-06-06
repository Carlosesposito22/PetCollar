package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class PassosAtivacao {

    private final ContextoCenario contexto;

    public PassosAtivacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("um atendimento em andamento com última interação do tutor há {int} minutos")
    public void atendimentoEmAndamentoComUltimaInteracao(int minutos) {
        ResumoAtendimento resumo = new ResumoAtendimento(
            contexto.atendimentoId, contexto.pacienteId, contexto.tutorId,
            LocalDateTime.now().minusMinutes(minutos), true);
        when(contexto.atendimentos.buscarResumo(contexto.atendimentoId))
            .thenReturn(java.util.Optional.of(resumo));
        when(contexto.atendimentos.listarEmAndamento()).thenReturn(List.of(resumo));
    }

    @E("um protocolo já ativo para o atendimento")
    public void protocoloJaAtivo() {
        contexto.protocoloAtivado();
    }

    @Quando("o sistema verifica a ativação do protocolo para o atendimento")
    public void verificaAtivacao() {
        try {
            contexto.ativacaoService.verificarEAtivar(contexto.atendimentoId)
                .ifPresent(p -> contexto.protocolo = p);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o sistema verifica novamente a ativação do protocolo")
    public void verificaNovamente() {
        verificaAtivacao();
    }

    @Entao("nenhum protocolo deve ser ativado")
    public void nenhumProtocoloAtivado() {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertTrue(contexto.protocoloRepositorio.listarAtivos().isEmpty(),
            "Não deveria haver protocolo ativo.");
    }

    @Entao("deve haver apenas um protocolo ativo para o atendimento")
    public void apenasUmProtocoloAtivo() {
        long ativos = contexto.protocoloRepositorio.listarAtivos().stream()
            .filter(p -> p.getAtendimentoId().equals(contexto.atendimentoId))
            .count();
        assertEquals(1, ativos, "Deveria existir exatamente um protocolo ativo.");
    }
}
