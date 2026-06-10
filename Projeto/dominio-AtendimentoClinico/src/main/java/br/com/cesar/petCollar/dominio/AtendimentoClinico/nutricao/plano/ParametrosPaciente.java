package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.math.BigDecimal;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.NivelAtividade;

/**
 * Parâmetros que o médico informa para alimentar a NEM (F-11). Validado em
 * construção: pesos positivos, densidade calórica positiva, comorbidade nunca
 * nula (use {@link Comorbidade#NENHUMA} se aplicável).
 */
public record ParametrosPaciente(
        BigDecimal pesoAtualKg,
        BigDecimal pesoIdealKg,
        NivelAtividade nivelAtividade,
        Comorbidade comorbidade,
        BigDecimal densidadeCaloricaKcalPorKg
) {
    public ParametrosPaciente {
        if (pesoAtualKg == null || pesoAtualKg.signum() <= 0)
            throw new IllegalArgumentException("Peso atual deve ser maior que zero.");
        if (pesoIdealKg == null || pesoIdealKg.signum() <= 0)
            throw new IllegalArgumentException("Peso ideal deve ser maior que zero.");
        if (nivelAtividade == null)
            throw new IllegalArgumentException("Nível de atividade é obrigatório.");
        if (comorbidade == null)
            throw new IllegalArgumentException("Comorbidade é obrigatória (use NENHUMA).");
        if (densidadeCaloricaKcalPorKg == null || densidadeCaloricaKcalPorKg.signum() <= 0)
            throw new IllegalArgumentException("Densidade calórica deve ser maior que zero.");
    }
}
