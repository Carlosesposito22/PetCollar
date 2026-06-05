package br.com.cesar.petCollar.dominio.AgendamentoClinico.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.BloqueioAgenda;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Passos compartilhados por mais de uma funcionalidade (montagem de prontuário e
 * agenda, asserções de erro/notificação e status da consulta). Mantidos numa única
 * classe para evitar definições de passo duplicadas no glue do Cucumber.
 */
public class PassosComuns {

    private final ContextoCenario contexto;

    public PassosComuns(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("um prontuário com status {string} para o paciente")
    public void prontuarioComStatus(String status) {
        when(contexto.prontuario.obterStatus(contexto.pacienteId))
            .thenReturn(StatusProntuario.valueOf(status));
    }

    @E("um horário livre na agenda do médico")
    public void horarioLivre() {
        contexto.horario = contexto.horarioUtilLivre();
        when(contexto.agendaRepositorio.obterExpediente(contexto.medicoId))
            .thenReturn(contexto.expedientePadrao());
        when(contexto.agendaRepositorio.listarBloqueios(eq(contexto.medicoId), any()))
            .thenReturn(List.of());
        when(contexto.consultaRepositorio.listarPorMedicoEPeriodo(eq(contexto.medicoId), any(), any()))
            .thenReturn(List.of());
    }

    @E("um horário ocupado na agenda do médico")
    public void horarioOcupado() {
        contexto.horario = contexto.horarioUtilLivre();
        when(contexto.agendaRepositorio.obterExpediente(contexto.medicoId))
            .thenReturn(contexto.expedientePadrao());
        when(contexto.agendaRepositorio.listarBloqueios(eq(contexto.medicoId), any()))
            .thenReturn(List.of(new BloqueioAgenda(contexto.horario, "Bloqueio administrativo")));
    }

    @E("o paciente já possui consulta no mesmo horário")
    public void pacienteJaPossuiConsulta() {
        when(contexto.consultaRepositorio.existeConflitoNoPaciente(eq(contexto.pacienteId), any()))
            .thenReturn(true);
    }

    @Entao("o agendamento deve ser recusado por regra de negócio")
    public void recusadoPorRegraDeNegocio() {
        assertNotNull(contexto.excecao, "Esperava-se uma exceção de regra de negócio.");
        assertInstanceOf(IllegalStateException.class, contexto.excecao);
    }

    @Entao("o agendamento deve ser recusado com erro de argumento")
    public void recusadoComErroDeArgumento() {
        assertNotNull(contexto.excecao, "Esperava-se uma exceção de argumento inválido.");
        assertInstanceOf(IllegalArgumentException.class, contexto.excecao);
    }

    @Entao("a consulta deve ficar com status {string}")
    public void consultaComStatus(String status) {
        assertNull(contexto.excecao, "Não deveria ter lançado exceção: " + contexto.excecao);
        assertEquals(StatusConsulta.valueOf(status), contexto.consulta.getStatus());
    }

    @E("o médico deve ser notificado")
    public void medicoNotificado() {
        verify(contexto.notificacao).notificarMedico(eq(contexto.medicoId), any());
    }

    @E("o tutor deve ser notificado")
    public void tutorNotificado() {
        verify(contexto.notificacao).notificarTutor(eq(contexto.tutorId), any());
    }
}
