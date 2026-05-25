package petcollar.dominio.beneficiosplano;

import java.util.Objects;
import java.util.UUID;

public final class BeneficioCatalogoId {

    private final String valor;

    private BeneficioCatalogoId(String valor) {
        this.valor = valor;
    }

    public static BeneficioCatalogoId gerar() {
        return new BeneficioCatalogoId(UUID.randomUUID().toString());
    }

    public static BeneficioCatalogoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("BeneficioCatalogoId não pode ser vazio.");
        }
        return new BeneficioCatalogoId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeneficioCatalogoId)) return false;
        BeneficioCatalogoId that = (BeneficioCatalogoId) o;
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

