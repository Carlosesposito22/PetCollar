package petcollar.dominio.farmacovigilancia;

import java.util.Objects;
import java.util.UUID;

public class ItemPrescricaoId {

    private final String valor;

    private ItemPrescricaoId(String valor) {
        this.valor = valor;
    }

    public static ItemPrescricaoId gerar() {
        return new ItemPrescricaoId(UUID.randomUUID().toString());
    }

    public static ItemPrescricaoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ItemPrescricaoId não pode ser vazio.");
        return new ItemPrescricaoId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPrescricaoId)) return false;
        return Objects.equals(valor, ((ItemPrescricaoId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
