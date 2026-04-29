package petcollar.dominio.farmacovigilancia;

import java.util.Objects;
import java.util.UUID;

public class MatrizInteracaoId {

    private final String valor;

    private MatrizInteracaoId(String valor) {
        this.valor = valor;
    }

    public static MatrizInteracaoId gerar() {
        return new MatrizInteracaoId(UUID.randomUUID().toString());
    }

    public static MatrizInteracaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("MatrizInteracaoId não pode ser vazio.");
        return new MatrizInteracaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatrizInteracaoId)) return false;
        return Objects.equals(valor, ((MatrizInteracaoId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
