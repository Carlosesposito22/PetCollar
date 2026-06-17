package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import java.time.LocalDateTime;

public record FiltroConsulta(StatusConsulta status, TipoConsulta tipo,
                             LocalDateTime inicio, LocalDateTime fim) {

    public static FiltroConsulta vazio() {
        return new FiltroConsulta(null, null, null, null);
    }

    public boolean correspondeStatus(StatusConsulta outro) {
        return status == null || status == outro;
    }

    public boolean correspondeTipo(TipoConsulta outro) {
        return tipo == null || tipo == outro;
    }

    public boolean correspondePeriodo(LocalDateTime instante) {
        if (inicio != null && instante.isBefore(inicio)) return false;
        if (fim != null && instante.isAfter(fim)) return false;
        return true;
    }
}
