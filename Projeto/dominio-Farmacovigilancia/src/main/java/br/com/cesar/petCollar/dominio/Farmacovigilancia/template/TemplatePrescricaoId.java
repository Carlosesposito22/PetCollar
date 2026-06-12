package br.com.cesar.petCollar.dominio.Farmacovigilancia.template;

import java.util.Objects;
import java.util.UUID;

public final class TemplatePrescricaoId {
    private final String valor;

    private TemplatePrescricaoId(String valor) { this.valor = valor; }

    public static TemplatePrescricaoId gerar() { return new TemplatePrescricaoId(UUID.randomUUID().toString()); }

    public static TemplatePrescricaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("TemplatePrescricaoId não pode ser vazio.");
        return new TemplatePrescricaoId(valor);
    }

    public String getValor() { return valor; }

    @Override public boolean equals(Object o) {
        return o instanceof TemplatePrescricaoId t && Objects.equals(valor, t.valor);
    }
    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
