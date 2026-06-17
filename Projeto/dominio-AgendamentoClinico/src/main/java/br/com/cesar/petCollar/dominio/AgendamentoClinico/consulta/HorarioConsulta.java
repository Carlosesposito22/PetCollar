package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class HorarioConsulta {

    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public HorarioConsulta(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null)
            throw new IllegalArgumentException("Início do horário não pode ser nulo.");
        if (fim == null)
            throw new IllegalArgumentException("Fim do horário não pode ser nulo.");
        if (!fim.isAfter(inicio))
            throw new IllegalArgumentException("O fim do horário deve ser posterior ao início.");
        this.inicio = inicio;
        this.fim = fim;
    }

    public boolean sobrepoeCom(HorarioConsulta outro) {
        if (outro == null)
            throw new IllegalArgumentException("Horário de comparação não pode ser nulo.");
        return this.inicio.isBefore(outro.fim) && outro.inicio.isBefore(this.fim);
    }

    public long calcularAntecedenciaEmHoras(LocalDateTime agora) {
        if (agora == null)
            throw new IllegalArgumentException("Instante de referência não pode ser nulo.");
        return Duration.between(agora, inicio).toHours();
    }

    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim()    { return fim; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HorarioConsulta)) return false;
        HorarioConsulta outro = (HorarioConsulta) o;
        return Objects.equals(inicio, outro.inicio) && Objects.equals(fim, outro.fim);
    }

    @Override
    public int hashCode() { return Objects.hash(inicio, fim); }

    @Override
    public String toString() { return inicio + " — " + fim; }
}
