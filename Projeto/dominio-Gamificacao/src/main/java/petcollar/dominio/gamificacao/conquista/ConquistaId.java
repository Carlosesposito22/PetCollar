package petcollar.dominio.gamificacao.conquista;

import java.util.Objects;
import java.util.UUID;

public class ConquistaId {

    private final String valor;

    private ConquistaId(String valor) {
        this.valor = valor;
    }

    public static ConquistaId gerar() {
        return new ConquistaId(UUID.randomUUID().toString());
    }

    public static ConquistaId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ConquistaId não pode ser vazio.");
        return new ConquistaId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConquistaId)) return false;
        return Objects.equals(valor, ((ConquistaId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
