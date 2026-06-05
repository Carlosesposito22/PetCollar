package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

public record MedicoDTO(String id) {

    public static MedicoDTO de(MedicoId medicoId) {
        return new MedicoDTO(medicoId.getValor());
    }
}
