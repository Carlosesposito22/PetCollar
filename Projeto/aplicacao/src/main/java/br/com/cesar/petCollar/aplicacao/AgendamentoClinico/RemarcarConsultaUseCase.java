package br.com.cesar.petCollar.aplicacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

public class RemarcarConsultaUseCase {

    private final GestaoAgendamentoService gestaoAgendamentoService;

    public RemarcarConsultaUseCase(GestaoAgendamentoService gestaoAgendamentoService) {
        if (gestaoAgendamentoService == null)
            throw new IllegalArgumentException("GestaoAgendamentoService é obrigatório.");
        this.gestaoAgendamentoService = gestaoAgendamentoService;
    }

    public Consulta executar(ConsultaId consultaId, HorarioConsulta novoHorario) {
        if (consultaId == null)
            throw new IllegalArgumentException("Id da consulta não pode ser nulo.");
        if (novoHorario == null)
            throw new IllegalArgumentException("Novo horário não pode ser nulo.");
        return gestaoAgendamentoService.remarcar(consultaId, novoHorario);
    }
}
