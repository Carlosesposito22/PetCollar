package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import java.util.Objects;
import java.util.UUID;

public final class VacinaId {

    private final String valor;

    private VacinaId(String valor) {
        this.valor = valor;
    }

    public static VacinaId gerar() {
        return new VacinaId(UUID.randomUUID().toString());
    }

    public static VacinaId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("VacinaId não pode ser vazio.");
        return new VacinaId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VacinaId v)) return false;
        return Objects.equals(valor, v.valor);
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
