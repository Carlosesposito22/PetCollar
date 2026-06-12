package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

import java.util.Objects;
import java.util.UUID;

/**
 * Identidade canônica de um {@link Medicamento} do catálogo de farmacovigilância.
 */
public final class MedicamentoId {
    private final String valor;

    private MedicamentoId(String valor) { this.valor = valor; }

    public static MedicamentoId gerar() { return new MedicamentoId(UUID.randomUUID().toString()); }

    public static MedicamentoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("MedicamentoId não pode ser vazio.");
        return new MedicamentoId(valor);
    }

    public String getValor() { return valor; }

    @Override public boolean equals(Object o) {
        return o instanceof MedicamentoId m && Objects.equals(valor, m.valor);
    }
    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
