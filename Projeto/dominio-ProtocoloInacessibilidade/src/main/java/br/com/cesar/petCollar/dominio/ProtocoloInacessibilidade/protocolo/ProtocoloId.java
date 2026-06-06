package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import java.util.Objects;
import java.util.UUID;

public final class ProtocoloId {

    private final String valor;

    private ProtocoloId(String valor) {
        this.valor = valor;
    }

    public static ProtocoloId gerar() {
        return new ProtocoloId(UUID.randomUUID().toString());
    }

    public static ProtocoloId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ProtocoloId não pode ser vazio.");
        return new ProtocoloId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProtocoloId)) return false;
        return Objects.equals(valor, ((ProtocoloId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
