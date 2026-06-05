package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

/**
 * <h2>Decorator abstrato</h2>
 *
 * Base comum para todos os decoradores: encapsula a referência para o componente
 * envolvido ({@link #base}) e exige que cada subclasse implemente
 * {@link CalculadoraValor#calcular()} aplicando sua regra sobre o resultado de
 * {@code base.calcular()}.
 */
public abstract class CalculadoraValorDecorator implements CalculadoraValor {

    protected final CalculadoraValor base;

    protected CalculadoraValorDecorator(CalculadoraValor base) {
        if (base == null)
            throw new IllegalArgumentException("Calculadora base do decorator não pode ser nula.");
        this.base = base;
    }
}
