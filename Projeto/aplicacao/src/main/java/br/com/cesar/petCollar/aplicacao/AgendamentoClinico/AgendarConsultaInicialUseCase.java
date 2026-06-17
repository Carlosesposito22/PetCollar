package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoConsultaInicialService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.RequisicaoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;

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
