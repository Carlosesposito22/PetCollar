package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <h2>ConcreteComponent do Decorator</h2>
 *
 * Folha da cadeia: devolve simplesmente o valor original da Cobrança, sem
 * modificações. É o ponto de partida que será envolvido pelos decoradores.
 */
public final class ValorBase implements CalculadoraValor {

    private final BigDecimal valor;

    public ValorBase(BigDecimal valor) {
        if (valor == null)
            throw new IllegalArgumentException("Valor base não pode ser nulo.");
        if (valor.signum() < 0)
            throw new IllegalArgumentException("Valor base não pode ser negativo.");
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcular() {
        return valor;
    }
}
