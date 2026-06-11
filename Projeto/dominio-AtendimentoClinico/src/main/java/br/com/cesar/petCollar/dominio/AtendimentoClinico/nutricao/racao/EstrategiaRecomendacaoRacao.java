package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;

/**
 * Strategy de pontuação de uma {@link Racao} para um determinado perfil de
 * paciente (porte, faixa etária, comorbidade). Cada implementação foca em
 * UM critério; a {@code RecomendacaoRacaoService} combina as pontuações.
 *
 * <p>Convenção: a pontuação devolvida é não-negativa. {@code 0} significa
 * "irrelevante" para este critério; valores maiores indicam maior afinidade.
 */
public interface EstrategiaRecomendacaoRacao {

    /** Identificador descritivo para diagnóstico/logs. */
    String nome();

    int pontuar(Racao racao, PerfilNutricional perfil);

    /**
     * Dados do paciente usados pelas Strategies — record interno aqui para não
     * vazar tipos do agregado de plano.
     */
    record PerfilNutricional(Porte porte, FaixaEtaria faixaEtaria, Comorbidade comorbidade) {
        public PerfilNutricional {
            if (porte == null) throw new IllegalArgumentException("Porte é obrigatório.");
            if (faixaEtaria == null) throw new IllegalArgumentException("Faixa etária é obrigatória.");
            if (comorbidade == null) throw new IllegalArgumentException("Comorbidade é obrigatória.");
        }
    }
}
