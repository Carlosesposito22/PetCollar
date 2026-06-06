package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import java.util.Objects;
import java.util.UUID;

public final class TentativaId {

    private final String valor;

    private TentativaId(String valor) {
        this.valor = valor;
    }

    public static TentativaId gerar() {
        return new TentativaId(UUID.randomUUID().toString());
    }

    public static TentativaId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("TentativaId não pode ser vazio.");
        return new TentativaId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TentativaId)) return false;
        return Objects.equals(valor, ((TentativaId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
