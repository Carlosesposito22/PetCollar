package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;

public interface EstrategiaRecomendacaoRacao {

    String nome();

    int pontuar(Racao racao, PerfilNutricional perfil);

    record PerfilNutricional(Porte porte, FaixaEtaria faixaEtaria, Comorbidade comorbidade) {
        public PerfilNutricional {
            if (porte == null) throw new IllegalArgumentException("Porte é obrigatório.");
            if (faixaEtaria == null) throw new IllegalArgumentException("Faixa etária é obrigatória.");
            if (comorbidade == null) throw new IllegalArgumentException("Comorbidade é obrigatória.");
        }
    }
}
