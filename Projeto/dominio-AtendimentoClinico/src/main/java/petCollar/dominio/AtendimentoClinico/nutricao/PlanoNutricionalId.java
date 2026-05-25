package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.Objects;
import java.util.UUID;

public class PlanoNutricionalId {

    private final String valor;

    private PlanoNutricionalId(String valor) {
        this.valor = valor;
    }

    public static PlanoNutricionalId gerar() {
        return new PlanoNutricionalId(UUID.randomUUID().toString());
    }

    public static PlanoNutricionalId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("PlanoNutricionalId não pode ser vazio.");
        return new PlanoNutricionalId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanoNutricionalId)) return false;
        return Objects.equals(valor, ((PlanoNutricionalId) o).valor);
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
