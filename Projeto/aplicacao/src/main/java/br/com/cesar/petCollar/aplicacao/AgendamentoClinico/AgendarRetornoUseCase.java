package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoRetornoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.RequisicaoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;

/**
 * Caso de uso de agendamento de consulta de retorno (F-05).
 * Delega ao Template Method {@link AgendamentoRetornoService}, que aplica os passos
 * invariantes e os variantes do retorno (consulta de origem elegível — RN 7; ao menos
 * um exame concluído — RN 10; vínculo com a origem — RN 11). Após o agendamento,
 * marca a consulta de origem como RETORNO_AGENDADO para bloquear novo retorno (RN 7).
 */
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

        // RN 7 — bloqueia novo retorno para a mesma consulta de origem.
        requisicao.getConsultaOrigemId().ifPresent((ConsultaId origemId) ->
            consultaRepositorio.buscarPorId(origemId).ifPresent(origem -> {
                origem.marcarRetornoAgendado();
                consultaRepositorio.salvar(origem);
            })
        );

        return retorno;
    }
}
