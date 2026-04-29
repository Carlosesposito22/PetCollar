package br.com.cesar.petCollar.dominio.compartilhado;

import java.util.Objects;
import java.util.UUID;

public final class AtendimentoId {

    private final String valor;

    private AtendimentoId(String valor) {
        this.valor = valor;
    }

    public static AtendimentoId gerar() {
        return new AtendimentoId(UUID.randomUUID().toString());
    }

    public static AtendimentoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("AtendimentoId não pode ser vazio.");
        }
        return new AtendimentoId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AtendimentoId)) return false;
        AtendimentoId that = (AtendimentoId) o;
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
