package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import java.util.Objects;
import java.util.UUID;

public final class ResponsavelSecundarioId {

    private final String valor;

    private ResponsavelSecundarioId(String valor) {
        this.valor = valor;
    }

    public static ResponsavelSecundarioId gerar() {
        return new ResponsavelSecundarioId(UUID.randomUUID().toString());
    }

    public static ResponsavelSecundarioId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ResponsavelSecundarioId não pode ser vazio.");
        return new ResponsavelSecundarioId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponsavelSecundarioId)) return false;
        return Objects.equals(valor, ((ResponsavelSecundarioId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
