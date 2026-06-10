package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <h2>ConcreteComponent do Decorator — F-11 NEM base</h2>
 *
 * Calcula a NEM base sem modificadores:
 * <pre>
 *   NEM_base = 70 × peso_metabolico
 *   peso_metabolico = peso_ideal<sup>0,75</sup>
 * </pre>
 *
 * É a folha da cadeia. Os decoradores envolvem este nó para aplicar fator
 * de atividade e modificador metabólico.
 */
public final class NEMBase implements CalculadoraNEM {

    /** Coeficiente metabólico canônico para mamíferos (RN 1). */
    public static final BigDecimal COEFICIENTE_BASE = new BigDecimal("70");

    /** Expoente do peso metabólico (Kleiber). */
    public static final double EXPOENTE_PESO_METABOLICO = 0.75;

    private final BigDecimal pesoIdealKg;

    public NEMBase(BigDecimal pesoIdealKg) {
        if (pesoIdealKg == null)
            throw new IllegalArgumentException("Peso ideal não pode ser nulo.");
        if (pesoIdealKg.signum() <= 0)
            throw new IllegalArgumentException("Peso ideal deve ser maior que zero.");
        this.pesoIdealKg = pesoIdealKg;
    }

    /** Componente isolado: peso<sup>0,75</sup>. Útil para exibir no breakdown da UI. */
    public BigDecimal pesoMetabolico() {
        double pm = Math.pow(pesoIdealKg.doubleValue(), EXPOENTE_PESO_METABOLICO);
        return new BigDecimal(pm, MathContext.DECIMAL64).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcular() {
        return COEFICIENTE_BASE.multiply(pesoMetabolico())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPesoIdealKg() { return pesoIdealKg; }
}
