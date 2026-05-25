package petcollar.dominio.beneficiosplano;

import java.util.Objects;
import java.util.UUID;

public final class BeneficioTutorId {

    private final String valor;

    private BeneficioTutorId(String valor) {
        this.valor = valor;
    }

    public static BeneficioTutorId gerar() {
        return new BeneficioTutorId(UUID.randomUUID().toString());
    }

    public static BeneficioTutorId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("BeneficioTutorId não pode ser vazio.");
        }
        return new BeneficioTutorId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeneficioTutorId)) return false;
        BeneficioTutorId that = (BeneficioTutorId) o;
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

