package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import java.time.LocalDateTime;

/**
 * Subentidade do agregado {@link Consulta}. Cada operação relevante (criar,
 * confirmar, remarcar, cancelar, realizar) gera um evento auditável (RN 19).
 */
public final class EventoAgendamento {

    private final TipoEventoAgendamento tipo;
    private final LocalDateTime ocorridoEm;
    private final String detalhe;

    public EventoAgendamento(TipoEventoAgendamento tipo, LocalDateTime ocorridoEm, String detalhe) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do evento não pode ser nulo.");
        if (ocorridoEm == null)
            throw new IllegalArgumentException("Instante do evento não pode ser nulo.");
        this.tipo = tipo;
        this.ocorridoEm = ocorridoEm;
        this.detalhe = detalhe;
    }

    public TipoEventoAgendamento getTipo() { return tipo; }
    public LocalDateTime getOcorridoEm()   { return ocorridoEm; }
    public String getDetalhe()             { return detalhe; }
}
