package petCollar.dominio.AtendimentoClinico.relatorio;

import java.time.LocalDateTime;
import java.util.Objects;

public final class SinaisVitais {

    private final double pesoKg;
    private final double temperaturaCelsius;
    private final int frequenciaCardiacaBpm;
    private final LocalDateTime aferidoEm;

    public SinaisVitais(double pesoKg, double temperaturaCelsius,
                        int frequenciaCardiacaBpm, LocalDateTime aferidoEm) {
        if (pesoKg <= 0)
            throw new IllegalArgumentException("Peso em kg deve ser maior que zero.");
        if (temperaturaCelsius <= 0)
            throw new IllegalArgumentException("Temperatura em Celsius deve ser maior que zero.");
        if (frequenciaCardiacaBpm <= 0)
            throw new IllegalArgumentException("Frequência cardíaca em bpm deve ser maior que zero.");
        if (aferidoEm == null)
            throw new IllegalArgumentException("Data/hora de aferição não pode ser nula.");

        this.pesoKg = pesoKg;
        this.temperaturaCelsius = temperaturaCelsius;
        this.frequenciaCardiacaBpm = frequenciaCardiacaBpm;
        this.aferidoEm = aferidoEm;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public double getTemperaturaCelsius() {
        return temperaturaCelsius;
    }

    public int getFrequenciaCardiacaBpm() {
        return frequenciaCardiacaBpm;
    }

    public LocalDateTime getAferidoEm() {
        return aferidoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SinaisVitais)) return false;
        SinaisVitais other = (SinaisVitais) o;
        return Double.compare(other.pesoKg, pesoKg) == 0
                && Double.compare(other.temperaturaCelsius, temperaturaCelsius) == 0
                && frequenciaCardiacaBpm == other.frequenciaCardiacaBpm
                && Objects.equals(aferidoEm, other.aferidoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pesoKg, temperaturaCelsius, frequenciaCardiacaBpm, aferidoEm);
    }
}
