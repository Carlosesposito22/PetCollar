package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

public abstract class CalculadoraNEMDecorator implements CalculadoraNEM {

    protected final CalculadoraNEM base;

    protected CalculadoraNEMDecorator(CalculadoraNEM base) {
        if (base == null)
            throw new IllegalArgumentException("Calculadora base do decorator não pode ser nula.");
        this.base = base;
    }
}
