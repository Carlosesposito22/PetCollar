package br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

public class AgendamentoConsultaInicialService extends AgendamentoService {

    public AgendamentoConsultaInicialService(
            IConsultaProntuario prontuario,
            IConsultaRepositorio consultaRepositorio,
            DisponibilidadeAgendaService disponibilidadeAgenda,
            IServicoNotificacao servicoNotificacao) {
        super(prontuario, consultaRepositorio, disponibilidadeAgenda, servicoNotificacao);
    }

    @Override
    protected void validarPreCondicoesEspecificas(RequisicaoAgendamento requisicao) {

        if (requisicao.getMotivo() == null || requisicao.getMotivo().getValor().isBlank())
            throw new IllegalArgumentException(
                    "O motivo da consulta é obrigatório para o agendamento inicial.");
    }

    @Override
    protected void executarValidacoesExtras(RequisicaoAgendamento requisicao) {

    }

    @Override
    protected Consulta criarConsulta(RequisicaoAgendamento requisicao) {
        return new Consulta(
                ConsultaId.gerar(),
                requisicao.getPacienteId(),
                requisicao.getTutorId(),
                requisicao.getMedicoId(),
                requisicao.getEspecialidadeId(),
                requisicao.getMotivo(),
                requisicao.getHorario());
    }
}
