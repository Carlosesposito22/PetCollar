package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;

import java.util.List;

public interface IConsultaExame {

    List<ExameResumo> listarPorConsultaOrigem(ConsultaId consultaOrigemId);

    long contarConcluidosPorConsultaOrigem(ConsultaId consultaOrigemId);

    void adicionar(ConsultaId consultaOrigemId, String descricao);

    void confirmar(String exameId);

    void registrarLaudo(String exameId, String laudo);
}
