package petcollar.dominio.gamificacao.conquista;

import java.util.Objects;
import java.util.UUID;

public class BadgeId {

    private final String valor;

    private BadgeId(String valor) {
        this.valor = valor;
    }

    public static BadgeId gerar() {
        return new BadgeId(UUID.randomUUID().toString());
    }

    public static BadgeId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("BadgeId não pode ser vazio.");
        return new BadgeId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BadgeId)) return false;
        return Objects.equals(valor, ((BadgeId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
