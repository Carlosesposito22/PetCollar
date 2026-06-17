package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoRetornoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.RequisicaoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;

public class AgendarRetornoUseCase {

    private final AgendamentoRetornoService agendamentoRetornoService;
    private final IConsultaRepositorio consultaRepositorio;

    public AgendarRetornoUseCase(AgendamentoRetornoService agendamentoRetornoService,
                                 IConsultaRepositorio consultaRepositorio) {
        if (agendamentoRetornoService == null)
            throw new IllegalArgumentException("AgendamentoRetornoService é obrigatório.");
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("IConsultaRepositorio é obrigatório.");
        this.agendamentoRetornoService = agendamentoRetornoService;
        this.consultaRepositorio = consultaRepositorio;
    }

    public Consulta executar(RequisicaoAgendamento requisicao) {
        if (requisicao == null)
            throw new IllegalArgumentException("Requisição de agendamento não pode ser nula.");

        Consulta retorno = agendamentoRetornoService.agendar(requisicao);

        requisicao.getConsultaOrigemId().ifPresent((ConsultaId origemId) ->
            consultaRepositorio.buscarPorId(origemId).ifPresent(origem -> {
                origem.marcarRetornoAgendado();
                consultaRepositorio.salvar(origem);
            })
        );

        return retorno;
    }
}
