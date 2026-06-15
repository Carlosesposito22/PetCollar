package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoConsultaInicialService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.RequisicaoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;

/**
 * Caso de uso de agendamento de consulta inicial (F-05).
 * Delega ao Template Method {@link AgendamentoConsultaInicialService}, que aplica
 * os passos invariantes (prontuário ativo, disponibilidade, conflito, notificações)
 * e os variantes específicos da consulta inicial (motivo obrigatório — RN 3).
 */
public class AgendarConsultaInicialUseCase {

    private final AgendamentoConsultaInicialService agendamentoService;

    public AgendarConsultaInicialUseCase(AgendamentoConsultaInicialService agendamentoService) {
        if (agendamentoService == null)
            throw new IllegalArgumentException("AgendamentoConsultaInicialService é obrigatório.");
        this.agendamentoService = agendamentoService;
    }

    public Consulta executar(RequisicaoAgendamento requisicao) {
        if (requisicao == null)
            throw new IllegalArgumentException("Requisição de agendamento não pode ser nula.");
        return agendamentoService.agendar(requisicao);
    }
}
