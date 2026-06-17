package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

public class EventoAuditoria {

    private final EventoAuditoriaId id;
    private final TipoEventoAuditoria tipo;

    private final TutorId tutorId;

    private final IndicacaoId indicacaoId;
    private final String descricao;
    private final LocalDateTime timestamp;

    public EventoAuditoria(EventoAuditoriaId id, TipoEventoAuditoria tipo,
                           TutorId tutorId, IndicacaoId indicacaoId,
                           String descricao) {
        if (id == null)       throw new IllegalArgumentException("Id do evento de auditoria não pode ser nulo.");
        if (tipo == null)     throw new IllegalArgumentException("Tipo do evento de auditoria não pode ser nulo.");
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição do evento de auditoria não pode ser vazia.");
        this.id = id;
        this.tipo = tipo;
        this.tutorId = tutorId;
        this.indicacaoId = indicacaoId;
        this.descricao = descricao;
        this.timestamp = LocalDateTime.now();
    }

    public EventoAuditoria(EventoAuditoriaId id, TipoEventoAuditoria tipo,
                           TutorId tutorId, IndicacaoId indicacaoId,
                           String descricao, LocalDateTime timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.tutorId = tutorId;
        this.indicacaoId = indicacaoId;
        this.descricao = descricao;
        this.timestamp = timestamp;
    }

    public EventoAuditoriaId getId()       { return id; }
    public TipoEventoAuditoria getTipo()   { return tipo; }
    public TutorId getTutorId()            { return tutorId; }
    public IndicacaoId getIndicacaoId()    { return indicacaoId; }
    public String getDescricao()           { return descricao; }
    public LocalDateTime getTimestamp()    { return timestamp; }
}
