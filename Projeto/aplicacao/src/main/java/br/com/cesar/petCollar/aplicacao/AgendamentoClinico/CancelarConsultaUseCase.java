package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;

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
