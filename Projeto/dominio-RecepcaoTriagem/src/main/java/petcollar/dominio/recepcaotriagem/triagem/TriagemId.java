package petcollar.dominio.recepcaotriagem.triagem;

import java.util.Objects;
import java.util.UUID;

public class TriagemId {

    private final String valor;

    private TriagemId(String valor) {
        this.valor = valor;
    }

    public static TriagemId gerar() {
        return new TriagemId(UUID.randomUUID().toString());
    }

    public static TriagemId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("TriagemId não pode ser vazio.");
        return new TriagemId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TriagemId)) return false;
        return Objects.equals(valor, ((TriagemId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
