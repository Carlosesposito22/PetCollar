package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

public record MedicoDTO(String id, String nome) {

    public static MedicoDTO de(MedicoId medicoId, String nome) {
        return new MedicoDTO(medicoId.getValor(), nome);
    }
}
