package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DescontoIndicacaoDecorator extends CalculadoraValorDecorator {

    private final BigDecimal valorDesconto;

    public DescontoIndicacaoDecorator(CalculadoraValor base, BigDecimal valorDesconto) {
        super(base);
        if (valorDesconto == null)
            throw new IllegalArgumentException("Valor do desconto não pode ser nulo.");
        if (valorDesconto.signum() < 0)
            throw new IllegalArgumentException("Valor do desconto não pode ser negativo.");
        this.valorDesconto = valorDesconto.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcular() {
        BigDecimal resultado = base.calcular().subtract(valorDesconto);
        if (resultado.signum() < 0) resultado = BigDecimal.ZERO;
        return resultado.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorDesconto() { return valorDesconto; }
}
