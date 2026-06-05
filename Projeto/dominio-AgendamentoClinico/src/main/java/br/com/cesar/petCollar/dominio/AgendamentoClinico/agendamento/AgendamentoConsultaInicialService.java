package br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

/**
 * Implementação concreta do Template Method para agendamento de CONSULTA INICIAL.
 *
 * <p>Implementa os três passos variantes de {@link AgendamentoService}:
 * <ul>
 *   <li>{@code validarPreCondicoesEspecificas}: motivo preenchido (RN 3);</li>
 *   <li>{@code executarValidacoesExtras}: nenhuma (gancho opcional vazio);</li>
 *   <li>{@code criarConsulta}: cria a {@link Consulta} do tipo INICIAL.</li>
 * </ul>
 */
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
        // RN 3 — motivo obrigatório na consulta inicial.
        if (requisicao.getMotivo() == null || requisicao.getMotivo().getValor().isBlank())
            throw new IllegalArgumentException(
                    "O motivo da consulta é obrigatório para o agendamento inicial.");
    }

    @Override
    protected void executarValidacoesExtras(RequisicaoAgendamento requisicao) {
        // Nenhuma validação extra para consulta inicial.
        // Gancho opcional vazio intencional — subclasses poderiam estender se necessário.
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
                requisicao.getHorario());   // construtor de consulta INICIAL
    }
}
