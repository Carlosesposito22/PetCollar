package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.util.Objects;
import java.util.UUID;

public final class RacaoId {
    private final String valor;

    private RacaoId(String valor) { this.valor = valor; }

    public static RacaoId gerar() { return new RacaoId(UUID.randomUUID().toString()); }

    public static RacaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("RacaoId não pode ser vazio.");
        return new RacaoId(valor);
    }

    public String getValor() { return valor; }

    @Override public boolean equals(Object o) {
        return o instanceof RacaoId r && Objects.equals(valor, r.valor);
    }
    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
