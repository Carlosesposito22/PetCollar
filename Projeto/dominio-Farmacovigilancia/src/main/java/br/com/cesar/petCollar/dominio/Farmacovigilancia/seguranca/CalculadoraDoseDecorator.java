package br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca;

/**
 * Base abstrata dos decorators do cálculo de dose máxima — mantém referência
 * ao componente que está sendo decorado para que cada camada acumule sobre
 * o resultado anterior.
 */
public abstract class CalculadoraDoseDecorator implements CalculadoraDoseMaximaSegura {

    protected final CalculadoraDoseMaximaSegura base;

    protected CalculadoraDoseDecorator(CalculadoraDoseMaximaSegura base) {
        if (base == null) throw new IllegalArgumentException("Componente base é obrigatório.");
        this.base = base;
    }
}
