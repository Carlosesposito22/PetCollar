package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.Especialidade;

public record EspecialidadeDTO(String id, String nome, String descricao) {

    public static EspecialidadeDTO de(Especialidade e) {
        return new EspecialidadeDTO(e.getId().getValor(), e.getNome(), e.getDescricao());
    }
}
