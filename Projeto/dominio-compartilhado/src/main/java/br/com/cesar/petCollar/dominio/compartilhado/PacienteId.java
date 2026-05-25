package br.com.cesar.petCollar.dominio.compartilhado;

import java.util.Objects;
import java.util.UUID;

public final class PacienteId {

    private final String valor;

    private PacienteId(String valor) {
        this.valor = valor;
    }

    public static PacienteId gerar() {
        return new PacienteId(UUID.randomUUID().toString());
    }

    public static PacienteId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("PacienteId não pode ser vazio.");
        }
        return new PacienteId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacienteId)) return false;
        PacienteId that = (PacienteId) o;
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
