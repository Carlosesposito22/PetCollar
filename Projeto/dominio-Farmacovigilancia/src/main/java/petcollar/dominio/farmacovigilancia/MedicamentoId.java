package petcollar.dominio.farmacovigilancia;

import java.util.Objects;
import java.util.UUID;

public class MedicamentoId {

    private final String valor;

    private MedicamentoId(String valor) {
        this.valor = valor;
    }

    public static MedicamentoId gerar() {
        return new MedicamentoId(UUID.randomUUID().toString());
    }

    public static MedicamentoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("MedicamentoId não pode ser vazio.");
        return new MedicamentoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicamentoId)) return false;
        return Objects.equals(valor, ((MedicamentoId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
