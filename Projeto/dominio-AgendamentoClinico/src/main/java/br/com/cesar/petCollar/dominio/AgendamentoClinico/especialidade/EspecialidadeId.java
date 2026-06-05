package br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade;

import java.util.Objects;
import java.util.UUID;

public final class EspecialidadeId {

    private final String valor;

    private EspecialidadeId(String valor) {
        this.valor = valor;
    }

    public static EspecialidadeId gerar() {
        return new EspecialidadeId(UUID.randomUUID().toString());
    }

    public static EspecialidadeId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("EspecialidadeId não pode ser vazio.");
        return new EspecialidadeId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EspecialidadeId)) return false;
        return Objects.equals(valor, ((EspecialidadeId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
