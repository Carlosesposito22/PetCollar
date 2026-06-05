package br.com.cesar.petCollar.dominio.AgendamentoClinico.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.EventoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.TipoEventoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class PassosRemarcacaoCancelamento {

    private final ContextoCenario contexto;

    public PassosRemarcacaoCancelamento(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("uma consulta confirmada com antecedência de {int} horas")
    public void consultaConfirmadaComAntecedencia(int horas) {
        LocalDateTime inicio = LocalDateTime.now().plusHours(horas);
        contexto.horario = new HorarioConsulta(inicio, inicio.plusMinutes(30));
        contexto.consulta = new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
            contexto.medicoId, EspecialidadeId.gerar(),
            MotivoConsulta.de("Consulta inicial de rotina"), contexto.horario);
        contexto.consulta.confirmar();
        when(contexto.consultaRepositorio.buscarPorId(contexto.consulta.getId()))
            .thenReturn(Optional.of(contexto.consulta));
    }

    @Quando("o tutor remarca a consulta para um novo horário")
    public void remarcaConsulta() {
        LocalDateTime novoInicio = LocalDateTime.now().plusHours(72);
        HorarioConsulta novo = new HorarioConsulta(novoInicio, novoInicio.plusMinutes(30));
        try {
            contexto.gestaoService.remarcar(contexto.consulta.getId(), novo);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o tutor cancela a consulta")
    public void cancelaConsulta() {
        try {
            contexto.gestaoService.cancelar(contexto.consulta.getId());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Entao("a consulta deve registrar {int} remarcação no histórico")
    public void consultaRegistraRemarcacoes(int esperado) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertEquals(esperado, contexto.consulta.getQuantidadeRemarcacoes());
        assertEquals(esperado, contexto.consulta.getHistoricoRemarcacoes().size());
    }

    @Entao("a consulta deve possuir um evento {string}")
    public void consultaPossuiEvento(String tipo) {
        TipoEventoAgendamento esperado = TipoEventoAgendamento.valueOf(tipo);
        boolean possui = contexto.consulta.getEventos().stream()
            .map(EventoAgendamento::getTipo)
            .anyMatch(esperado::equals);
        assertTrue(possui, "A consulta deveria possuir um evento " + tipo);
    }
}
