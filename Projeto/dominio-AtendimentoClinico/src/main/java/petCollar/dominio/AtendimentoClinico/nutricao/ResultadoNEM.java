package petCollar.dominio.AtendimentoClinico.nutricao;

import java.time.LocalDate;
import java.util.Objects;

public final class ResultadoNEM {

    private final double kcalDiarias;
    private final double gramasDiariasTotais;
    private final int numeroDeRefeicoesDia;
    private final LocalDate dataPrevistaAtingirPesoIdeal;

    public ResultadoNEM(double kcalDiarias, double gramasDiariasTotais,
                       int numeroDeRefeicoesDia, LocalDate dataPrevistaAtingirPesoIdeal) {
        if (kcalDiarias < 0)
            throw new IllegalArgumentException("Kcal diárias não podem ser negativas.");
        if (gramasDiariasTotais < 0)
            throw new IllegalArgumentException("Gramas diárias não podem ser negativas.");
        if (numeroDeRefeicoesDia < 0)
            throw new IllegalArgumentException("Número de refeições não pode ser negativo.");

        this.kcalDiarias = kcalDiarias;
        this.gramasDiariasTotais = gramasDiariasTotais;
        this.numeroDeRefeicoesDia = numeroDeRefeicoesDia;
        this.dataPrevistaAtingirPesoIdeal = dataPrevistaAtingirPesoIdeal;
    }

    public double getKcalDiarias() {
        return kcalDiarias;
    }

    public double getGramasDiariasTotais() {
        return gramasDiariasTotais;
    }

    public int getNumeroDeRefeicoesDia() {
        return numeroDeRefeicoesDia;
    }

    public LocalDate getDataPrevistaAtingirPesoIdeal() {
        return dataPrevistaAtingirPesoIdeal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultadoNEM)) return false;
        ResultadoNEM that = (ResultadoNEM) o;
        return Double.compare(that.kcalDiarias, kcalDiarias) == 0
                && Double.compare(that.gramasDiariasTotais, gramasDiariasTotais) == 0
                && numeroDeRefeicoesDia == that.numeroDeRefeicoesDia
                && Objects.equals(dataPrevistaAtingirPesoIdeal, that.dataPrevistaAtingirPesoIdeal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kcalDiarias, gramasDiariasTotais, numeroDeRefeicoesDia, dataPrevistaAtingirPesoIdeal);
    }

    @Override
    public String toString() {
        return "ResultadoNEM{" +
                "kcalDiarias=" + kcalDiarias +
                ", gramasDiariasTotais=" + gramasDiariasTotais +
                ", numeroDeRefeicoesDia=" + numeroDeRefeicoesDia +
                ", dataPrevistaAtingirPesoIdeal=" + dataPrevistaAtingirPesoIdeal +
                '}';
    }
}
