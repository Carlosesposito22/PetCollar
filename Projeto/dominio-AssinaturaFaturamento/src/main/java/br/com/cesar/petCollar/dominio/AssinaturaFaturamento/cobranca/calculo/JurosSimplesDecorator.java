package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <h2>ConcreteDecorator — Juros simples diários</h2>
 *
 * Aplica a regra de juros simples definida em RN 4 (F-07):
 *     juros = valorBase × taxaDiaria × diasAtraso
 *
 * A taxa canônica do petCollar é {@link #TAXA_PADRAO} (0,033% ao dia). O cálculo
 * incide sobre o resultado da decoração inferior (que pode já trazer descontos).
 */
public final class JurosSimplesDecorator extends CalculadoraValorDecorator {

    /** Taxa diária canônica do petCollar (0,033% a.d.) — referência: F-07 RN 4. */
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

    /** Componente do valor — útil para apresentar o breakdown ao tutor. */
    public BigDecimal apenasJuros() {
        return base.calcular()
                .multiply(taxaDiaria)
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public int getDiasAtraso()        { return diasAtraso; }
    public BigDecimal getTaxaDiaria() { return taxaDiaria; }
}
