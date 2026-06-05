package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HistoricoRemarcacao;

import java.time.LocalDateTime;

public record HistoricoRemarcacaoDTO(LocalDateTime anteriorInicio, LocalDateTime anteriorFim,
                                     LocalDateTime novoInicio, LocalDateTime novoFim,
                                     LocalDateTime remarcadoEm) {

    public static HistoricoRemarcacaoDTO de(HistoricoRemarcacao h) {
        return new HistoricoRemarcacaoDTO(
            h.getHorarioAnterior().getInicio(), h.getHorarioAnterior().getFim(),
            h.getHorarioNovo().getInicio(), h.getHorarioNovo().getFim(),
            h.getRemarcadoEm());
    }
}
