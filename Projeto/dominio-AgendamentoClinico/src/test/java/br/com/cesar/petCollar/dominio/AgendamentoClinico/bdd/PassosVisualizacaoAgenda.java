package br.com.cesar.petCollar.dominio.AgendamentoClinico.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.FiltroConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PassosVisualizacaoAgenda {

    private final ContextoCenario contexto;

    public PassosVisualizacaoAgenda(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("o paciente possui consultas registradas na agenda")
    public void pacientePossuiConsultas() {
        Consulta agendada = novaConsulta();
        Consulta confirmada = novaConsulta();
        confirmada.confirmar();
        contexto.consultasDoPaciente = List.of(agendada, confirmada);

        when(contexto.consultaRepositorio.listarPorPaciente(eq(contexto.pacienteId), any()))
            .thenAnswer(invocacao -> {
                FiltroConsulta filtro = invocacao.getArgument(1);
                return contexto.consultasDoPaciente.stream()
                    .filter(c -> filtro.correspondeStatus(c.getStatus()))
                    .filter(c -> filtro.correspondeTipo(c.getTipo()))
                    .filter(c -> filtro.correspondePeriodo(c.getCriadaEm()))
                    .toList();
            });
    }

    @Quando("o tutor lista a agenda filtrando pelo status {string}")
    public void listaAgendaPorStatus(String status) {
        FiltroConsulta filtro = new FiltroConsulta(StatusConsulta.valueOf(status), null, null, null);
        contexto.consultasResultantes =
            contexto.consultaRepositorio.listarPorPaciente(contexto.pacienteId, filtro);
    }

    @Entao("devem ser listadas apenas consultas com status {string}")
    public void apenasConsultasComStatus(String status) {
        StatusConsulta esperado = StatusConsulta.valueOf(status);
        assertFalse(contexto.consultasResultantes.isEmpty(), "Esperava-se ao menos uma consulta.");
        assertTrue(contexto.consultasResultantes.stream().allMatch(c -> c.getStatus() == esperado),
            "Todas as consultas filtradas deveriam ter status " + status);
    }

    private Consulta novaConsulta() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(3);
        return new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
            contexto.medicoId, EspecialidadeId.gerar(),
            MotivoConsulta.de("Consulta de acompanhamento"),
            new HorarioConsulta(inicio, inicio.plusMinutes(30)));
    }
}
