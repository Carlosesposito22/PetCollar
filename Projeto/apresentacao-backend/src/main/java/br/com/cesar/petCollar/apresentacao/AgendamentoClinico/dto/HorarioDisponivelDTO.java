package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

import java.time.LocalDateTime;

public record HorarioDisponivelDTO(LocalDateTime inicio, LocalDateTime fim) {

    public static HorarioDisponivelDTO de(HorarioConsulta h) {
        return new HorarioDisponivelDTO(h.getInicio(), h.getFim());
    }
}
