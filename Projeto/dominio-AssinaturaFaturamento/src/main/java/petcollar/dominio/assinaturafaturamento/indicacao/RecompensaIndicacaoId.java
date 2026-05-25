package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class RecompensaIndicacaoId {

    private final String valor;

    private RecompensaIndicacaoId(String valor) {
        this.valor = valor;
    }

    public static RecompensaIndicacaoId gerar() {
        return new RecompensaIndicacaoId(UUID.randomUUID().toString());
    }

    public static RecompensaIndicacaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("RecompensaIndicacaoId não pode ser vazio.");
        return new RecompensaIndicacaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecompensaIndicacaoId)) return false;
        return Objects.equals(valor, ((RecompensaIndicacaoId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
