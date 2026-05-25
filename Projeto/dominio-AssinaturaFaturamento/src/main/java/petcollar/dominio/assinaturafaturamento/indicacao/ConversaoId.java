package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class ConversaoId {

    private final String valor;

    private ConversaoId(String valor) {
        this.valor = valor;
    }

    public static ConversaoId gerar() {
        return new ConversaoId(UUID.randomUUID().toString());
    }

    public static ConversaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ConversaoId não pode ser vazio.");
        return new ConversaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversaoId)) return false;
        return Objects.equals(valor, ((ConversaoId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
