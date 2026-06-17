package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

public abstract class CalculadoraValorDecorator implements CalculadoraValor {

    protected final CalculadoraValor base;

    protected CalculadoraValorDecorator(CalculadoraValor base) {
        if (base == null)
            throw new IllegalArgumentException("Calculadora base do decorator não pode ser nula.");
        this.base = base;
    }
}
