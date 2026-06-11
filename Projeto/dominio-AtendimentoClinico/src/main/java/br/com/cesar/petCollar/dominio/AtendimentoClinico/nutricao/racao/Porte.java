package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.math.BigDecimal;

/**
 * Porte do paciente, derivado do peso ideal (kg). É usado pela
 * recomendação de ração para filtrar fórmulas específicas.
 */
public enum Porte {
    PEQUENO, MEDIO, GRANDE;

    public static Porte de(BigDecimal pesoIdealKg) {
        if (pesoIdealKg == null)
            throw new IllegalArgumentException("Peso ideal é obrigatório para derivar o porte.");
        double kg = pesoIdealKg.doubleValue();
        if (kg <= 0)
            throw new IllegalArgumentException("Peso ideal deve ser positivo.");
        if (kg < 10) return PEQUENO;
        if (kg <= 25) return MEDIO;
        return GRANDE;
    }
}
