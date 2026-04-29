package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.Objects;

public final class PesoIdeal {

    private final double pesoAtualKg;
    private final double pesoIdealKg;
    private final double divergenciaPercentual;

    public PesoIdeal(double pesoAtualKg, double pesoIdealKg) {
        if (pesoAtualKg <= 0)
            throw new IllegalArgumentException("Peso atual deve ser maior que zero.");
        if (pesoIdealKg <= 0)
            throw new IllegalArgumentException("Peso ideal deve ser maior que zero.");

        this.pesoAtualKg = pesoAtualKg;
        this.pesoIdealKg = pesoIdealKg;
        this.divergenciaPercentual = Math.abs(pesoAtualKg - pesoIdealKg) / pesoIdealKg * 100.0;
    }

    public double getPesoAtualKg() {
        return pesoAtualKg;
    }

    public double getPesoIdealKg() {
        return pesoIdealKg;
    }

    public double getDivergenciaPercentual() {
        return divergenciaPercentual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PesoIdeal)) return false;
        PesoIdeal pesoIdeal = (PesoIdeal) o;
        return Double.compare(pesoIdeal.pesoAtualKg, pesoAtualKg) == 0
                && Double.compare(pesoIdeal.pesoIdealKg, pesoIdealKg) == 0
                && Double.compare(pesoIdeal.divergenciaPercentual, divergenciaPercentual) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pesoAtualKg, pesoIdealKg, divergenciaPercentual);
    }

    @Override
    public String toString() {
        return "PesoIdeal{" +
                "pesoAtualKg=" + pesoAtualKg +
                ", pesoIdealKg=" + pesoIdealKg +
                ", divergenciaPercentual=" + divergenciaPercentual +
                '}';
    }
}
