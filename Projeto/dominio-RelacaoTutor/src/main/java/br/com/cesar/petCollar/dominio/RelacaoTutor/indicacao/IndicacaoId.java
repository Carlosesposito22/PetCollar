package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class IndicacaoId {

    private final String valor;

    private IndicacaoId(String valor) { this.valor = valor; }

    public static IndicacaoId gerar() { return new IndicacaoId(UUID.randomUUID().toString()); }

    public static IndicacaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("IndicacaoId não pode ser vazio.");
        return new IndicacaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndicacaoId other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
