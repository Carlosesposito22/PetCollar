package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;

public record ExameDTO(String exameId, String descricao, String status) {

    public static ExameDTO de(ExameResumo e) {
        return new ExameDTO(e.exameId(), e.descricao(), e.status().name());
    }
}
