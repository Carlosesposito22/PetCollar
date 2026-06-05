package br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

/**
 * Value Object que representa um período indisponível na agenda de um médico
 * (férias, bloqueio administrativo, etc.) usado pelo cálculo de disponibilidade (RN 4).
 */
public final class BloqueioAgenda {

    private final HorarioConsulta periodo;
    private final String motivo;

    public BloqueioAgenda(HorarioConsulta periodo, String motivo) {
        if (periodo == null)
            throw new IllegalArgumentException("Período do bloqueio não pode ser nulo.");
        this.periodo = periodo;
        this.motivo = motivo;
    }

    public HorarioConsulta getPeriodo() { return periodo; }
    public String getMotivo()           { return motivo; }
}
