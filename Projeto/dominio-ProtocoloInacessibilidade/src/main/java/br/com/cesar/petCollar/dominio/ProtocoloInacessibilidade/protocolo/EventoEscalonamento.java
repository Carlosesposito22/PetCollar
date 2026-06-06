package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import java.time.LocalDateTime;

/**
 * Subentidade do agregado {@link ProtocoloInacessibilidade}. Cada avanço de nível
 * de escalonamento gera um evento auditável (RN 7): o {@link NivelEscalonamento}
 * atingido, o motivo, o responsável acionado e o instante.
 */
public final class EventoEscalonamento {

    private final EventoEscalonamentoId id;
    private final NivelEscalonamento nivel;
    private final String motivo;
    private final String responsavelAcionadoId;   // referência cross-agregado (pode ser nulo)
    private final LocalDateTime ocorridoEm;

    public EventoEscalonamento(EventoEscalonamentoId id, NivelEscalonamento nivel, String motivo,
                               String responsavelAcionadoId, LocalDateTime ocorridoEm) {
        if (id == null)
            throw new IllegalArgumentException("Id do evento de escalonamento não pode ser nulo.");
        if (nivel == null)
            throw new IllegalArgumentException("Nível de escalonamento não pode ser nulo.");
        if (ocorridoEm == null)
            throw new IllegalArgumentException("Instante do escalonamento não pode ser nulo.");
        this.id = id;
        this.nivel = nivel;
        this.motivo = motivo == null ? "" : motivo;
        this.responsavelAcionadoId = responsavelAcionadoId;
        this.ocorridoEm = ocorridoEm;
    }

    public EventoEscalonamentoId getId()      { return id; }
    public NivelEscalonamento getNivel()      { return nivel; }
    public String getMotivo()                 { return motivo; }
    public String getResponsavelAcionadoId()  { return responsavelAcionadoId; }
    public LocalDateTime getOcorridoEm()      { return ocorridoEm; }
}
