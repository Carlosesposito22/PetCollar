package petcollar.dominio.farmacovigilancia;

import java.util.Objects;
import java.util.UUID;

public class PrescricaoId {

    private final String valor;

    private PrescricaoId(String valor) {
        this.valor = valor;
    }

    public static PrescricaoId gerar() {
        return new PrescricaoId(UUID.randomUUID().toString());
    }

    public static PrescricaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("PrescricaoId não pode ser vazio.");
        return new PrescricaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrescricaoId)) return false;
        return Objects.equals(valor, ((PrescricaoId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
