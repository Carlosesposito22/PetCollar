package br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;

public abstract class AgendamentoService {

    private final IConsultaProntuario prontuario;
    private final IConsultaRepositorio consultaRepositorio;
    private final DisponibilidadeAgendaService disponibilidadeAgenda;
    private final IServicoNotificacao servicoNotificacao;

    protected AgendamentoService(
            IConsultaProntuario prontuario,
            IConsultaRepositorio consultaRepositorio,
            DisponibilidadeAgendaService disponibilidadeAgenda,
            IServicoNotificacao servicoNotificacao) {
        if (prontuario == null)
            throw new IllegalArgumentException("Porta de prontuário não pode ser nula.");
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("Repositório de consulta não pode ser nulo.");
        if (disponibilidadeAgenda == null)
            throw new IllegalArgumentException("Serviço de disponibilidade não pode ser nulo.");
        if (servicoNotificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.prontuario = prontuario;
        this.consultaRepositorio = consultaRepositorio;
        this.disponibilidadeAgenda = disponibilidadeAgenda;
        this.servicoNotificacao = servicoNotificacao;
    }

    public final Consulta agendar(RequisicaoAgendamento requisicao) {
        if (requisicao == null)
            throw new IllegalArgumentException("Requisição de agendamento não pode ser nula.");

        validarProntuarioAtivo(requisicao.getPacienteId());

        validarPreCondicoesEspecificas(requisicao);

        validarDisponibilidadeNaAgenda(requisicao.getMedicoId(), requisicao.getHorario());

        validarAusenciaDeConflito(requisicao.getPacienteId(), requisicao.getHorario());

        executarValidacoesExtras(requisicao);

        Consulta consulta = criarConsulta(requisicao);
        consulta.confirmar();
        consultaRepositorio.salvar(consulta);

        notificarMedico(consulta);

        notificarTutor(consulta);

        return consulta;
    }

    private void validarProntuarioAtivo(PacienteId pacienteId) {
        if (prontuario.obterStatus(pacienteId) != StatusProntuario.ATIVO)
            throw new IllegalStateException(
                    "O agendamento só é permitido para pacientes com prontuário ativo.");
    }

    private void validarDisponibilidadeNaAgenda(MedicoId medicoId, HorarioConsulta horario) {
        if (!disponibilidadeAgenda.estaDisponivel(medicoId, horario))
            throw new IllegalStateException(
                    "O médico não possui disponibilidade no horário solicitado.");
    }

    private void validarAusenciaDeConflito(PacienteId pacienteId, HorarioConsulta horario) {
        if (consultaRepositorio.existeConflitoNoPaciente(pacienteId, horario))
            throw new IllegalStateException(
                    "O paciente já possui consulta agendada neste horário.");
    }

    private void notificarMedico(Consulta consulta) {
        String mensagem = "Consulta " + consulta.getTipo() + " confirmada para "
                + consulta.getHorario()
                + (consulta.getConsultaOrigemId() != null
                    ? " (retorno da consulta de origem " + consulta.getConsultaOrigemId().getValor()
                      + ", com exames vinculados)"
                    : "")
                + ".";
        servicoNotificacao.notificarMedico(consulta.getMedicoId(), mensagem);
    }

    private void notificarTutor(Consulta consulta) {
        servicoNotificacao.notificarTutor(consulta.getTutorId(),
                "Sua consulta " + consulta.getTipo() + " foi confirmada para "
                + consulta.getHorario() + ".");
    }

    protected abstract void validarPreCondicoesEspecificas(RequisicaoAgendamento requisicao);

    protected abstract void executarValidacoesExtras(RequisicaoAgendamento requisicao);

    protected abstract Consulta criarConsulta(RequisicaoAgendamento requisicao);
}
