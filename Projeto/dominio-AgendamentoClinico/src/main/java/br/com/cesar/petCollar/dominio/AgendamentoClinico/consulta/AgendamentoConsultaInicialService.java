package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;

/**
 * Serviço de domínio que orquestra o agendamento de uma consulta inicial:
 * valida prontuário ativo (RN 1), disponibilidade da agenda (RN 4) e ausência de
 * conflito no paciente (RN 5); confirma a consulta e notifica médico (RN 6) e
 * tutor (RN 14). Stateless; dependências por construtor.
 */
public class AgendamentoConsultaInicialService {

    private final IConsultaRepositorio consultaRepositorio;
    private final IConsultaProntuario prontuario;
    private final DisponibilidadeAgendaService disponibilidade;
    private final IServicoNotificacao notificacao;

    public AgendamentoConsultaInicialService(IConsultaRepositorio consultaRepositorio,
                                             IConsultaProntuario prontuario,
                                             DisponibilidadeAgendaService disponibilidade,
                                             IServicoNotificacao notificacao) {
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("Repositório de consultas não pode ser nulo.");
        if (prontuario == null)
            throw new IllegalArgumentException("Porta de prontuário não pode ser nula.");
        if (disponibilidade == null)
            throw new IllegalArgumentException("Serviço de disponibilidade não pode ser nulo.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.consultaRepositorio = consultaRepositorio;
        this.prontuario = prontuario;
        this.disponibilidade = disponibilidade;
        this.notificacao = notificacao;
    }

    public Consulta agendar(Consulta consulta) {
        if (consulta == null)
            throw new IllegalArgumentException("Consulta não pode ser nula.");
        if (consulta.getTipo() != TipoConsulta.INICIAL)
            throw new IllegalArgumentException("Este serviço agenda apenas consultas iniciais.");

        // RN 1 — prontuário ativo
        if (prontuario.obterStatus(consulta.getPacienteId()) != StatusProntuario.ATIVO)
            throw new IllegalStateException(
                "Não é possível agendar: o prontuário do paciente não está ativo.");

        // RN 4 — disponibilidade da agenda do médico
        if (!disponibilidade.estaDisponivel(consulta.getMedicoId(), consulta.getHorario()))
            throw new IllegalStateException(
                "O horário escolhido não está disponível na agenda do médico.");

        // RN 5 — conflito de horário no próprio paciente
        if (consultaRepositorio.existeConflitoNoPaciente(consulta.getPacienteId(), consulta.getHorario()))
            throw new IllegalStateException(
                "O paciente já possui uma consulta que se sobrepõe a esse horário.");

        consulta.confirmar();                       // RN 6 — confirmação inicial
        consultaRepositorio.salvar(consulta);

        // RN 6 — notifica o médico; RN 14 — notifica o tutor
        notificacao.notificarMedico(consulta.getMedicoId(),
            "Nova consulta inicial confirmada para o horário " + consulta.getHorario() + ".");
        notificacao.notificarTutor(consulta.getTutorId(),
            "Sua consulta inicial foi confirmada para " + consulta.getHorario() + ".");

        return consulta;
    }
}
