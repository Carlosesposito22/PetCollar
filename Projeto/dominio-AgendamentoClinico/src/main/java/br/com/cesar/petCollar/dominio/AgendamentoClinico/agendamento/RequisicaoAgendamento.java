package br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import java.util.Optional;

public final class RequisicaoAgendamento {

    private final PacienteId pacienteId;
    private final TutorId tutorId;
    private final MedicoId medicoId;
    private final EspecialidadeId especialidadeId;
    private final MotivoConsulta motivo;
    private final HorarioConsulta horario;
    private final ConsultaId consultaOrigemId;

    public RequisicaoAgendamento(PacienteId pacienteId, TutorId tutorId,
            MedicoId medicoId, EspecialidadeId especialidadeId,
            MotivoConsulta motivo, HorarioConsulta horario) {
        this(pacienteId, tutorId, medicoId, especialidadeId, motivo, horario, null);
    }

    public RequisicaoAgendamento(PacienteId pacienteId, TutorId tutorId,
            MedicoId medicoId, EspecialidadeId especialidadeId,
            MotivoConsulta motivo, HorarioConsulta horario,
            ConsultaId consultaOrigemId) {
        if (pacienteId == null) throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        if (tutorId == null) throw new IllegalArgumentException("TutorId não pode ser nulo.");
        if (medicoId == null) throw new IllegalArgumentException("MedicoId não pode ser nulo.");
        if (especialidadeId == null) throw new IllegalArgumentException("EspecialidadeId não pode ser nulo.");
        if (horario == null) throw new IllegalArgumentException("Horário não pode ser nulo.");
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoId = medicoId;
        this.especialidadeId = especialidadeId;
        this.motivo = motivo;
        this.horario = horario;
        this.consultaOrigemId = consultaOrigemId;
    }

    public PacienteId getPacienteId()             { return pacienteId; }
    public TutorId getTutorId()                   { return tutorId; }
    public MedicoId getMedicoId()                 { return medicoId; }
    public EspecialidadeId getEspecialidadeId()   { return especialidadeId; }
    public MotivoConsulta getMotivo()             { return motivo; }
    public HorarioConsulta getHorario()           { return horario; }

    public Optional<ConsultaId> getConsultaOrigemId() {
        return Optional.ofNullable(consultaOrigemId);
    }
}
