package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class JurosSimplesDecorator extends CalculadoraValorDecorator {

    public static final BigDecimal TAXA_PADRAO = new BigDecimal("0.00033");

    private final int diasAtraso;
    private final BigDecimal taxaDiaria;

    public JurosSimplesDecorator(CalculadoraValor base, int diasAtraso, BigDecimal taxaDiaria) {
        super(base);
        if (diasAtraso < 0)
            throw new IllegalArgumentException("Dias de atraso não pode ser negativo.");
        if (taxaDiaria == null || taxaDiaria.signum() < 0)
            throw new IllegalArgumentException("Taxa diária inválida.");
        this.diasAtraso = diasAtraso;
        this.taxaDiaria = taxaDiaria;
    }

    public JurosSimplesDecorator(CalculadoraValor base, int diasAtraso) {
        this(base, diasAtraso, TAXA_PADRAO);
    }

    @Override
    public BigDecimal calcular() {
        BigDecimal valorBase = base.calcular();
        BigDecimal juros = valorBase
                .multiply(taxaDiaria)
                .multiply(BigDecimal.valueOf(diasAtraso));
        return valorBase.add(juros).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal apenasJuros() {
        return base.calcular()
                .multiply(taxaDiaria)
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public int getDiasAtraso()        { return diasAtraso; }
    public BigDecimal getTaxaDiaria() { return taxaDiaria; }
}
