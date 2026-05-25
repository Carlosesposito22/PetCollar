package br.com.cesar.petCollar.dominio.compartilhado;

import java.util.Objects;
import java.util.UUID;

public final class PlanoId {

    private final String valor;

    private PlanoId(String valor) {
        this.valor = valor;
    }

    public static PlanoId gerar() {
        return new PlanoId(UUID.randomUUID().toString());
    }

    public static PlanoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("PlanoId não pode ser vazio.");
        }
        return new PlanoId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanoId)) return false;
        PlanoId planoId = (PlanoId) o;
        return Objects.equals(valor, planoId.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}

