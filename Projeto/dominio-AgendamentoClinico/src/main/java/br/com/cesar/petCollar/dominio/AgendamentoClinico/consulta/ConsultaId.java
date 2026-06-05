package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import java.util.Objects;
import java.util.UUID;

public final class ConsultaId {

    private final String valor;

    private ConsultaId(String valor) {
        this.valor = valor;
    }

    public static ConsultaId gerar() {
        return new ConsultaId(UUID.randomUUID().toString());
    }

    public static ConsultaId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ConsultaId não pode ser vazio.");
        return new ConsultaId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultaId)) return false;
        return Objects.equals(valor, ((ConsultaId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
