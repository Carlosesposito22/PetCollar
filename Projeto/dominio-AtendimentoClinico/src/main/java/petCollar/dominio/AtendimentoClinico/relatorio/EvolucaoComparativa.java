package petCollar.dominio.AtendimentoClinico.relatorio;

import java.util.Objects;

public final class EvolucaoComparativa {

    private final double variacaoPesoKg;
    private final String resumoTextual;

    public EvolucaoComparativa(double variacaoPesoKg, String resumoTextual) {
        if (resumoTextual == null || resumoTextual.isBlank())
            throw new IllegalArgumentException("Resumo textual da evolução não pode ser vazio.");

        this.variacaoPesoKg = variacaoPesoKg;
        this.resumoTextual = resumoTextual;
    }

    public double getVariacaoPesoKg() {
        return variacaoPesoKg;
    }

    public String getResumoTextual() {
        return resumoTextual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvolucaoComparativa)) return false;
        EvolucaoComparativa other = (EvolucaoComparativa) o;
        return Double.compare(other.variacaoPesoKg, variacaoPesoKg) == 0
                && Objects.equals(resumoTextual, other.resumoTextual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variacaoPesoKg, resumoTextual);
    }
}
