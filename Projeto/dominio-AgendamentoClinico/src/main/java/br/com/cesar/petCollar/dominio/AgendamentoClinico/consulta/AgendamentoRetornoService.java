package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;

/**
 * Serviço de domínio que orquestra o agendamento de uma consulta de retorno:
 * valida prontuário ativo (RN 1), elegibilidade da consulta de origem (RN 7),
 * presença de ao menos um exame concluído (RN 10) e ausência de conflito no
 * paciente (RN 5); confirma e notifica médico (RN 13) e tutor (RN 14).
 */
public class AgendamentoRetornoService {

    private final IConsultaRepositorio consultaRepositorio;
    private final IConsultaProntuario prontuario;
    private final IConsultaExame exames;
    private final IServicoNotificacao notificacao;

    public AgendamentoRetornoService(IConsultaRepositorio consultaRepositorio,
                                     IConsultaProntuario prontuario,
                                     IConsultaExame exames,
                                     IServicoNotificacao notificacao) {
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("Repositório de consultas não pode ser nulo.");
        if (prontuario == null)
            throw new IllegalArgumentException("Porta de prontuário não pode ser nula.");
        if (exames == null)
            throw new IllegalArgumentException("Porta de exames não pode ser nula.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.consultaRepositorio = consultaRepositorio;
        this.prontuario = prontuario;
        this.exames = exames;
        this.notificacao = notificacao;
    }

    public Consulta agendar(Consulta retorno) {
        if (retorno == null)
            throw new IllegalArgumentException("Consulta de retorno não pode ser nula.");
        if (retorno.getTipo() != TipoConsulta.RETORNO)
            throw new IllegalArgumentException("Este serviço agenda apenas consultas de retorno.");

        ConsultaId origemId = retorno.getConsultaOrigemId();   // RN 11 — garantido no construtor

        // RN 1 — prontuário ativo
        if (prontuario.obterStatus(retorno.getPacienteId()) != StatusProntuario.ATIVO)
            throw new IllegalStateException(
                "Não é possível agendar o retorno: o prontuário do paciente não está ativo.");

        // RN 7 — a consulta de origem precisa estar elegível a retorno
        Consulta origem = consultaRepositorio.buscarPorId(origemId)
            .orElseThrow(() -> new IllegalArgumentException("Consulta de origem não encontrada."));
        if (!origem.isElegivelParaRetorno())
            throw new IllegalStateException(
                "A consulta de origem não está elegível para retorno.");

        // RN 10 — bloqueia o retorno sem ao menos um exame concluído
        if (exames.contarConcluidosPorConsultaOrigem(origemId) < 1)
            throw new IllegalStateException(
                "É necessário ao menos um exame concluído na consulta de origem para liberar o retorno.");

        // RN 5 — conflito de horário no próprio paciente
        if (consultaRepositorio.existeConflitoNoPaciente(retorno.getPacienteId(), retorno.getHorario()))
            throw new IllegalStateException(
                "O paciente já possui uma consulta que se sobrepõe a esse horário.");

        retorno.confirmar();
        consultaRepositorio.salvar(retorno);

        // RN 13 — notifica o médico com vínculo aos exames; RN 14 — notifica o tutor
        notificacao.notificarMedico(retorno.getMedicoId(),
            "Retorno confirmado para " + retorno.getHorario()
            + ". Exames da consulta de origem " + origemId.getValor() + " disponíveis.");
        notificacao.notificarTutor(retorno.getTutorId(),
            "Seu retorno foi confirmado para " + retorno.getHorario() + ".");

        return retorno;
    }
}
