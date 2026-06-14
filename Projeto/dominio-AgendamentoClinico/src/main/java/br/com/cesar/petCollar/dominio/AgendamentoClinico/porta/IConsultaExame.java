package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;

import java.util.List;

/**
 * Porta de saída (anticorrupção) para os exames diagnósticos vinculados a uma
 * consulta de origem (contexto AtendimentoClinico). Suporta a exibição (RN 8), a
 * confirmação pelo tutor / anexação de laudo (RN 9, RN 12) e a regra de liberação
 * do retorno (RN 10).
 */
public interface IConsultaExame {

    List<ExameResumo> listarPorConsultaOrigem(ConsultaId consultaOrigemId);

    long contarConcluidosPorConsultaOrigem(ConsultaId consultaOrigemId);

    /** Médico solicita um exame para o retorno (chamado ao liberar/finalizar consulta com exames). */
    void adicionar(ConsultaId consultaOrigemId, String descricao);

    /** Tutor confirma a realização do exame, levando-o a CONCLUIDO (RN 9). */
    void confirmar(String exameId);

    /** Tutor anexa o laudo do exame, marcando-o como concluído (RN 9). */
    void registrarLaudo(String exameId, String laudo);
}
