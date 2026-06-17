package br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

public final class Expediente {

    private final LocalTime horaInicio;
    private final LocalTime horaFim;
    private final int duracaoConsultaMinutos;
    private final Set<DayOfWeek> diasAtendimento;

    public Expediente(LocalTime horaInicio, LocalTime horaFim, int duracaoConsultaMinutos,
                      Set<DayOfWeek> diasAtendimento) {
        if (horaInicio == null || horaFim == null)
            throw new IllegalArgumentException("Início e fim do expediente são obrigatórios.");
        if (!horaFim.isAfter(horaInicio))
            throw new IllegalArgumentException("O fim do expediente deve ser posterior ao início.");
        if (duracaoConsultaMinutos <= 0)
            throw new IllegalArgumentException("A duração da consulta deve ser positiva.");
        if (diasAtendimento == null || diasAtendimento.isEmpty())
            throw new IllegalArgumentException("É necessário ao menos um dia de atendimento.");
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.duracaoConsultaMinutos = duracaoConsultaMinutos;
        this.diasAtendimento = EnumSet.copyOf(diasAtendimento);
    }

    public boolean atendeNoDia(DayOfWeek dia) {
        return diasAtendimento.contains(dia);
    }

    public LocalTime getHoraInicio()         { return horaInicio; }
    public LocalTime getHoraFim()            { return horaFim; }
    public int getDuracaoConsultaMinutos()   { return duracaoConsultaMinutos; }
    public Set<DayOfWeek> getDiasAtendimento() { return EnumSet.copyOf(diasAtendimento); }
}
