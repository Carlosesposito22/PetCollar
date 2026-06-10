package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

/**
 * <h2>Decorator abstrato — F-11 NEM</h2>
 *
 * Base comum para todos os decoradores: encapsula a referência ao componente
 * envolvido ({@link #base}) e exige que cada subclasse implemente
 * {@link CalculadoraNEM#calcular()} aplicando sua regra sobre o resultado de
 * {@code base.calcular()}.
 */
public abstract class CalculadoraNEMDecorator implements CalculadoraNEM {

    protected final CalculadoraNEM base;

    protected CalculadoraNEMDecorator(CalculadoraNEM base) {
        if (base == null)
            throw new IllegalArgumentException("Calculadora base do decorator não pode ser nula.");
        this.base = base;
    }
}
