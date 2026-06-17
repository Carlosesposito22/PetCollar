package br.com.cesar.petCollar.dominio.AgendamentoClinico.bdd;

import org.mockito.Mockito;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.Expediente;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.IAgendaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoConsultaInicialService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoRetornoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.IEspecialidadeRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

public class ContextoCenario {

    public final IConsultaRepositorio consultaRepositorio = Mockito.mock(IConsultaRepositorio.class);
    public final IEspecialidadeRepositorio especialidadeRepositorio = Mockito.mock(IEspecialidadeRepositorio.class);
    public final IAgendaRepositorio agendaRepositorio = Mockito.mock(IAgendaRepositorio.class);
    public final IConsultaProntuario prontuario = Mockito.mock(IConsultaProntuario.class);
    public final IConsultaExame exames = Mockito.mock(IConsultaExame.class);
    public final IServicoNotificacao notificacao = Mockito.mock(IServicoNotificacao.class);

    public final DisponibilidadeAgendaService disponibilidade =
        new DisponibilidadeAgendaService(consultaRepositorio, agendaRepositorio);
    public final AgendamentoConsultaInicialService inicialService =
        new AgendamentoConsultaInicialService(prontuario, consultaRepositorio, disponibilidade, notificacao);
    public final AgendamentoRetornoService retornoService =
        new AgendamentoRetornoService(prontuario, consultaRepositorio, disponibilidade, notificacao, exames);
    public final GestaoAgendamentoService gestaoService =
        new GestaoAgendamentoService(consultaRepositorio, notificacao);

    public final PacienteId pacienteId = PacienteId.gerar();
    public final TutorId tutorId = TutorId.gerar();
    public final MedicoId medicoId = MedicoId.gerar();
    public EspecialidadeId especialidadeId;

    public HorarioConsulta horario;
    public Consulta consulta;
    public Consulta origem;
    public ConsultaId origemId;
    public List<MedicoId> medicosRetornados;
    public List<ExameResumo> examesRetornados;
    public List<Consulta> consultasResultantes;
    public List<Consulta> consultasDoPaciente;
    public Exception excecao;

    public Expediente expedientePadrao() {
        return new Expediente(LocalTime.of(8, 0), LocalTime.of(18, 0), 30,
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                       DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    }

    public HorarioConsulta horarioUtilLivre() {
        LocalDate dia = LocalDate.now().plusDays(1);
        while (dia.getDayOfWeek() != DayOfWeek.MONDAY) {
            dia = dia.plusDays(1);
        }
        return new HorarioConsulta(dia.atTime(9, 0), dia.atTime(9, 30));
    }
}
