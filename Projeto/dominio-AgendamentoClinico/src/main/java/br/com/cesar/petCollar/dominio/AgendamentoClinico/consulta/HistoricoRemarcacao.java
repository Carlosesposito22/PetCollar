package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import java.time.LocalDateTime;

public final class HistoricoRemarcacao {

    private final HorarioConsulta horarioAnterior;
    private final HorarioConsulta horarioNovo;
    private final LocalDateTime remarcadoEm;

    public HistoricoRemarcacao(HorarioConsulta horarioAnterior, HorarioConsulta horarioNovo,
                               LocalDateTime remarcadoEm) {
        if (horarioAnterior == null)
            throw new IllegalArgumentException("Horário anterior não pode ser nulo.");
        if (horarioNovo == null)
            throw new IllegalArgumentException("Horário novo não pode ser nulo.");
        if (remarcadoEm == null)
            throw new IllegalArgumentException("Instante da remarcação não pode ser nulo.");
        this.horarioAnterior = horarioAnterior;
        this.horarioNovo = horarioNovo;
        this.remarcadoEm = remarcadoEm;
    }

    public HorarioConsulta getHorarioAnterior() { return horarioAnterior; }
    public HorarioConsulta getHorarioNovo()     { return horarioNovo; }
    public LocalDateTime getRemarcadoEm()       { return remarcadoEm; }
}
