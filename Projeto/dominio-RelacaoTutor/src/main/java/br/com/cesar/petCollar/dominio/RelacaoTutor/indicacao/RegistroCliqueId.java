package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class RegistroCliqueId {

    private final String valor;

    private RegistroCliqueId(String valor) { this.valor = valor; }

    public static RegistroCliqueId gerar() { return new RegistroCliqueId(UUID.randomUUID().toString()); }

    public static RegistroCliqueId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("RegistroCliqueId não pode ser vazio.");
        return new RegistroCliqueId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistroCliqueId other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
