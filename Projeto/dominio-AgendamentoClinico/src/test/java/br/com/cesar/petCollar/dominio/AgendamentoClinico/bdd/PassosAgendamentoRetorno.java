package br.com.cesar.petCollar.dominio.AgendamentoClinico.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusExame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PassosAgendamentoRetorno {

    private final ContextoCenario contexto;
    private String exameId;

    public PassosAgendamentoRetorno(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Quando("o tutor tenta criar um retorno sem consulta de origem")
    public void criaRetornoSemOrigem() {
        try {
            new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
                contexto.medicoId, EspecialidadeId.gerar(),
                MotivoConsulta.de("Retorno de acompanhamento"), contexto.horarioUtilLivre(),
                null);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Dado("uma consulta de origem elegível a retorno")
    public void origemElegivel() {
        contexto.origem = novaConsultaInicial();
        contexto.origem.confirmar();
        contexto.origem.marcarComoRealizada();
        contexto.origem.solicitarExames();   // -> EXAMES_SOLICITADOS (elegível)
        contexto.origemId = contexto.origem.getId();
        when(contexto.consultaRepositorio.buscarPorId(contexto.origemId))
            .thenReturn(Optional.of(contexto.origem));
    }

    @Dado("uma consulta de origem não elegível a retorno")
    public void origemNaoElegivel() {
        contexto.origem = novaConsultaInicial();
        contexto.origem.confirmar();          // -> CONFIRMADA (não elegível)
        contexto.origemId = contexto.origem.getId();
        when(contexto.consultaRepositorio.buscarPorId(contexto.origemId))
            .thenReturn(Optional.of(contexto.origem));
    }

    @E("a consulta de origem não possui exames concluídos")
    public void origemSemExamesConcluidos() {
        when(contexto.exames.contarConcluidosPorConsultaOrigem(contexto.origemId)).thenReturn(0L);
    }

    @E("a consulta de origem possui {int} exame concluído")
    public void origemComExamesConcluidos(int quantidade) {
        when(contexto.exames.contarConcluidosPorConsultaOrigem(contexto.origemId))
            .thenReturn((long) quantidade);
    }

    @Quando("o tutor agenda o retorno")
    public void agendaRetorno() {
        contexto.consulta = new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
            contexto.medicoId, EspecialidadeId.gerar(),
            MotivoConsulta.de("Retorno de acompanhamento"), contexto.horarioUtilLivre(),
            contexto.origemId);
        try {
            contexto.retornoService.agendar(contexto.consulta);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Dado("uma consulta de origem com {int} exames solicitados")
    public void origemComExamesSolicitados(int quantidade) {
        contexto.origemId = ConsultaId.gerar();
        List<ExameResumo> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            lista.add(new ExameResumo("EX-" + i, "Exame " + i, StatusExame.SOLICITADO));
        }
        when(contexto.exames.listarPorConsultaOrigem(contexto.origemId)).thenReturn(lista);
    }

    @Quando("o tutor consulta os exames solicitados")
    public void consultaExamesSolicitados() {
        contexto.examesRetornados = contexto.exames.listarPorConsultaOrigem(contexto.origemId);
    }

    @Entao("devem ser exibidos {int} exames")
    public void devemSerExibidosExames(int esperado) {
        assertEquals(esperado, contexto.examesRetornados.size());
    }

    @Dado("um exame solicitado {string}")
    public void umExameSolicitado(String id) {
        this.exameId = id;
    }

    @Quando("o tutor confirma a realização do exame")
    public void confirmaExame() {
        contexto.exames.confirmar(exameId);
    }

    @Entao("a porta de exames deve registrar a confirmação do exame")
    public void portaRegistraConfirmacao() {
        verify(contexto.exames).confirmar(exameId);
    }

    private Consulta novaConsultaInicial() {
        return new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
            contexto.medicoId, EspecialidadeId.gerar(),
            MotivoConsulta.de("Consulta inicial de rotina"), contexto.horarioUtilLivre());
    }
}
