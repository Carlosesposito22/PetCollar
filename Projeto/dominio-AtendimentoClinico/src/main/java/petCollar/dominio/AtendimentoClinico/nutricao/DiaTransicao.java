package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.Objects;

public final class DiaTransicao {

    private final int numeroDia;
    private final double percentualDietaNova;
    private final double percentualDietaAntiga;

    public DiaTransicao(int numeroDia, double percentualDietaNova, double percentualDietaAntiga) {
        if (numeroDia < 1 || numeroDia > 7)
            throw new IllegalArgumentException("Número do dia deve estar entre 1 e 7.");
        if (percentualDietaNova < 0 || percentualDietaNova > 100)
            throw new IllegalArgumentException("Percentual de dieta nova deve estar entre 0 e 100.");
        if (percentualDietaAntiga < 0 || percentualDietaAntiga > 100)
            throw new IllegalArgumentException("Percentual de dieta antiga deve estar entre 0 e 100.");

        double soma = percentualDietaNova + percentualDietaAntiga;
        if (Math.abs(soma - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                "Soma dos percentuais deve ser 100. Recebido: " + soma);
        }

        this.numeroDia = numeroDia;
        this.percentualDietaNova = percentualDietaNova;
        this.percentualDietaAntiga = percentualDietaAntiga;
    }

    public int getNumeroDia() {
        return numeroDia;
    }

    public double getPercentualDietaNova() {
        return percentualDietaNova;
    }

    public double getPercentualDietaAntiga() {
        return percentualDietaAntiga;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiaTransicao)) return false;
        DiaTransicao that = (DiaTransicao) o;
        return numeroDia == that.numeroDia
                && Double.compare(that.percentualDietaNova, percentualDietaNova) == 0
                && Double.compare(that.percentualDietaAntiga, percentualDietaAntiga) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroDia, percentualDietaNova, percentualDietaAntiga);
    }

    @Override
    public String toString() {
        return "DiaTransicao{" +
                "numeroDia=" + numeroDia +
                ", percentualDietaNova=" + percentualDietaNova +
                ", percentualDietaAntiga=" + percentualDietaAntiga +
                '}';
    }
}
