package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class NEMBase implements CalculadoraNEM {

    public static final BigDecimal COEFICIENTE_BASE = new BigDecimal("70");

    public static final double EXPOENTE_PESO_METABOLICO = 0.75;

    private final BigDecimal pesoIdealKg;

    public NEMBase(BigDecimal pesoIdealKg) {
        if (pesoIdealKg == null)
            throw new IllegalArgumentException("Peso ideal não pode ser nulo.");
        if (pesoIdealKg.signum() <= 0)
            throw new IllegalArgumentException("Peso ideal deve ser maior que zero.");
        this.pesoIdealKg = pesoIdealKg;
    }

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
