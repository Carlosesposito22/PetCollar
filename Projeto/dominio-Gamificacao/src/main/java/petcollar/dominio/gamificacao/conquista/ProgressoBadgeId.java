package petcollar.dominio.gamificacao.conquista;

import java.util.Objects;
import java.util.UUID;

public class ProgressoBadgeId {

    private final String valor;

    private ProgressoBadgeId(String valor) {
        this.valor = valor;
    }

    public static ProgressoBadgeId gerar() {
        return new ProgressoBadgeId(UUID.randomUUID().toString());
    }

    public static ProgressoBadgeId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ProgressoBadgeId não pode ser vazio.");
        return new ProgressoBadgeId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgressoBadgeId)) return false;
        return Objects.equals(valor, ((ProgressoBadgeId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
