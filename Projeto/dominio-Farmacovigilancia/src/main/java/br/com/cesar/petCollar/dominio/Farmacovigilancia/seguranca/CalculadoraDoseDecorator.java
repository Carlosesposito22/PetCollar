package br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca;

public abstract class CalculadoraDoseDecorator implements CalculadoraDoseMaximaSegura {

    protected final CalculadoraDoseMaximaSegura base;

    protected CalculadoraDoseDecorator(CalculadoraDoseMaximaSegura base) {
        if (base == null) throw new IllegalArgumentException("Componente base é obrigatório.");
        this.base = base;
    }
}
