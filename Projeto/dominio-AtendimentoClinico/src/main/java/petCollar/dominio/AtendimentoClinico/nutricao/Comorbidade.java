package petCollar.dominio.AtendimentoClinico.nutricao;
import java.util.Objects;

public final class Comorbidade {

    private final TipoComorbidade tipo;
    private final double modificadorMetabolico;

    public Comorbidade(TipoComorbidade tipo, double modificadorMetabolico) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de comorbidade não pode ser nulo.");
        if (modificadorMetabolico <= 0)
            throw new IllegalArgumentException("Modificador metabólico deve ser maior que zero.");

        this.tipo = tipo;
        this.modificadorMetabolico = modificadorMetabolico;
    }

    public TipoComorbidade getTipo() {
        return tipo;
    }

    public double getModificadorMetabolico() {
        return modificadorMetabolico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comorbidade)) return false;
        Comorbidade that = (Comorbidade) o;
        return Double.compare(that.modificadorMetabolico, modificadorMetabolico) == 0
                && tipo == that.tipo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, modificadorMetabolico);
    }

    @Override
    public String toString() {
        return "Comorbidade{" +
                "tipo=" + tipo +
                ", modificadorMetabolico=" + modificadorMetabolico +
                '}';
    }
}
