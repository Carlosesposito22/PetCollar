package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class LinkIndicacaoId {

    private final String valor;

    private LinkIndicacaoId(String valor) { this.valor = valor; }

    public static LinkIndicacaoId gerar() { return new LinkIndicacaoId(UUID.randomUUID().toString()); }

    public static LinkIndicacaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("LinkIndicacaoId não pode ser vazio.");
        return new LinkIndicacaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkIndicacaoId other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
