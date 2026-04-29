package br.com.cesar.petCollar.dominio.compartilhado;

import java.util.Objects;
import java.util.UUID;

public final class MedicoId {

    private final String valor;

    private MedicoId(String valor) {
        this.valor = valor;
    }

    public static MedicoId gerar() {
        return new MedicoId(UUID.randomUUID().toString());
    }

    public static MedicoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("MedicoId não pode ser vazio.");
        }
        return new MedicoId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicoId)) return false;
        MedicoId that = (MedicoId) o;
        return Objects.equals(valor, that.valor);
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
