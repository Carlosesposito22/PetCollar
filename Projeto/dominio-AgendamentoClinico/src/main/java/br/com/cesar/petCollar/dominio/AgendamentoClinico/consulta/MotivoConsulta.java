package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import java.util.Objects;

public final class MotivoConsulta {

    private static final int COMPRIMENTO_MINIMO = 5;

    private final String valor;

    private MotivoConsulta(String valor) {
        this.valor = valor;
    }

    public static MotivoConsulta de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("Motivo da consulta é obrigatório.");
        String limpo = valor.trim();
        if (limpo.length() < COMPRIMENTO_MINIMO)
            throw new IllegalArgumentException(
                "Motivo da consulta deve ter ao menos " + COMPRIMENTO_MINIMO + " caracteres.");
        return new MotivoConsulta(limpo);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MotivoConsulta)) return false;
        return Objects.equals(valor, ((MotivoConsulta) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
