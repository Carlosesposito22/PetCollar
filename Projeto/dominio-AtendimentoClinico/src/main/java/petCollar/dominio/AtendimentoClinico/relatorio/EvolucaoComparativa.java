package petCollar.dominio.AtendimentoClinico.relatorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EvolucaoComparativa {

    private final double variacaoPesoKg;
    private final double variacaoTemperaturaCelsius;
    private final String resumoTextual;
    private final List<RegistroHistoricoVital> registrosHistorico;

    public EvolucaoComparativa(double variacaoPesoKg, double variacaoTemperaturaCelsius,
                               String resumoTextual, List<RegistroHistoricoVital> registrosHistorico) {
        if (resumoTextual == null || resumoTextual.isBlank())
            throw new IllegalArgumentException("Resumo textual da evolução não pode ser vazio.");
        this.variacaoPesoKg = variacaoPesoKg;
        this.variacaoTemperaturaCelsius = variacaoTemperaturaCelsius;
        this.resumoTextual = resumoTextual;
        this.registrosHistorico = registrosHistorico != null
            ? new ArrayList<>(registrosHistorico) : new ArrayList<>();
    }

    public double getVariacaoPesoKg() { return variacaoPesoKg; }
    public double getVariacaoTemperaturaCelsius() { return variacaoTemperaturaCelsius; }
    public String getResumoTextual() { return resumoTextual; }
    public List<RegistroHistoricoVital> getRegistrosHistorico() {
        return Collections.unmodifiableList(registrosHistorico);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvolucaoComparativa)) return false;
        EvolucaoComparativa other = (EvolucaoComparativa) o;
        return Double.compare(other.variacaoPesoKg, variacaoPesoKg) == 0
                && Double.compare(other.variacaoTemperaturaCelsius, variacaoTemperaturaCelsius) == 0
                && Objects.equals(resumoTextual, other.resumoTextual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variacaoPesoKg, variacaoTemperaturaCelsius, resumoTextual);
    }

    public record RegistroHistoricoVital(String data, double pesoKg, double temperaturaCelsius) {}
}
