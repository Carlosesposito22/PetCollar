package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class EventoAuditoriaId {

    private final String valor;

    private EventoAuditoriaId(String valor) { this.valor = valor; }

    public static EventoAuditoriaId gerar() { return new EventoAuditoriaId(UUID.randomUUID().toString()); }

    public static EventoAuditoriaId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("EventoAuditoriaId não pode ser vazio.");
        return new EventoAuditoriaId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventoAuditoriaId other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
