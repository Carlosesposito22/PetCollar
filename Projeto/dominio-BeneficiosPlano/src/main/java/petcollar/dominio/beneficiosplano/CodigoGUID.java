package petcollar.dominio.beneficiosplano;

import java.util.Objects;
import java.util.UUID;

public final class CodigoGUID {

    private final String valor;

    private CodigoGUID(String valor) {
        this.valor = valor;
    }

    public static CodigoGUID gerar() {
        return new CodigoGUID(UUID.randomUUID().toString());
    }

    public static CodigoGUID de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("CodigoGUID não pode ser vazio.");
        }
        return new CodigoGUID(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoGUID)) return false;
        CodigoGUID that = (CodigoGUID) o;
        return Objects.equals(valor, that.valor);
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

