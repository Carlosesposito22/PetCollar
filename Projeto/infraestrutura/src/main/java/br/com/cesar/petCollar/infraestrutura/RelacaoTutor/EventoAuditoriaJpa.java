package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.EventoAuditoria;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.EventoAuditoriaId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.TipoEventoAuditoria;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_auditoria_indicacao")
public class EventoAuditoriaJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tipo;

    private String tutorId;

    private String indicacaoId;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected EventoAuditoriaJpa() {}

    public static EventoAuditoriaJpa fromDomain(EventoAuditoria e) {
        EventoAuditoriaJpa j = new EventoAuditoriaJpa();
        j.id = e.getId().getValor();
        j.tipo = e.getTipo().name();
        j.tutorId = e.getTutorId() != null ? e.getTutorId().getValor() : null;
        j.indicacaoId = e.getIndicacaoId() != null ? e.getIndicacaoId().getValor() : null;
        j.descricao = e.getDescricao();
        j.timestamp = e.getTimestamp();
        return j;
    }

    public EventoAuditoria toDomain() {
        return new EventoAuditoria(
            EventoAuditoriaId.de(id),
            TipoEventoAuditoria.valueOf(tipo),
            tutorId != null ? TutorId.de(tutorId) : null,
            indicacaoId != null ? IndicacaoId.de(indicacaoId) : null,
            descricao,
            timestamp
        );
    }

    public String getId()       { return id; }
    public String getTutorId()  { return tutorId; }
    public String getIndicacaoId() { return indicacaoId; }
}
