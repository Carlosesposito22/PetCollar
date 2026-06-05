package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca;

import java.util.Objects;
import java.util.UUID;

public final class CobrancaId {

    private final String valor;

    private CobrancaId(String valor) { this.valor = valor; }

    public static CobrancaId gerar() { return new CobrancaId(UUID.randomUUID().toString()); }

    public static CobrancaId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("CobrancaId não pode ser vazio.");
        return new CobrancaId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CobrancaId other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
