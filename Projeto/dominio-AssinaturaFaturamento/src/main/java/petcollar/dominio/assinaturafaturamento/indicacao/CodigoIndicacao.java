package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.Objects;
import java.util.UUID;

public final class CodigoIndicacao {

    private final String valor;

    private CodigoIndicacao(String valor) {
        this.valor = valor;
    }

    public static CodigoIndicacao gerar() {
        String codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return new CodigoIndicacao(codigo);
    }

    public static CodigoIndicacao de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("CodigoIndicacao não pode ser vazio.");
        if (!valor.matches("[A-Za-z0-9]+"))
            throw new IllegalArgumentException("CodigoIndicacao deve ser alfanumérico.");
        return new CodigoIndicacao(valor.toUpperCase());
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoIndicacao)) return false;
        return Objects.equals(valor, ((CodigoIndicacao) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
