package petcollar.dominio.beneficiosplano;

import java.util.Objects;
import java.util.UUID;

public final class TicketBeneficioId {

    private final String valor;

    private TicketBeneficioId(String valor) {
        this.valor = valor;
    }

    public static TicketBeneficioId gerar() {
        return new TicketBeneficioId(UUID.randomUUID().toString());
    }

    public static TicketBeneficioId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("TicketBeneficioId não pode ser vazio.");
        }
        return new TicketBeneficioId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketBeneficioId)) return false;
        TicketBeneficioId that = (TicketBeneficioId) o;
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

