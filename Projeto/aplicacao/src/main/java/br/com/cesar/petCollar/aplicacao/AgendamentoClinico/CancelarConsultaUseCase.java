package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;

/**
 * Caso de uso de cancelamento de consulta (inicial ou de retorno) — F-05 RN 16/19.
 * Garante antecedência mínima de 24 horas e registra evento auditável,
 * delegando ao {@link GestaoAgendamentoService}.
 */
public class CancelarConsultaUseCase {

    private final GestaoAgendamentoService gestaoAgendamentoService;

    public CancelarConsultaUseCase(GestaoAgendamentoService gestaoAgendamentoService) {
        if (gestaoAgendamentoService == null)
            throw new IllegalArgumentException("GestaoAgendamentoService é obrigatório.");
        this.gestaoAgendamentoService = gestaoAgendamentoService;
    }

    public Consulta executar(ConsultaId consultaId) {
        if (consultaId == null)
            throw new IllegalArgumentException("Id da consulta não pode ser nulo.");
        return gestaoAgendamentoService.cancelar(consultaId);
    }
}
