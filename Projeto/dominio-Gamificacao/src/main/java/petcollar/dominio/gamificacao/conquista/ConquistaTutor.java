package petcollar.dominio.gamificacao.conquista;

import java.time.LocalDateTime;

public class ConquistaTutor {

    private final ConquistaId id;
    private final String tutorId;
    private final BadgeId badgeId;
    private final LocalDateTime conquistadoEm;

    // Construtor de CRIAÇÃO
    public ConquistaTutor(ConquistaId id, String tutorId, BadgeId badgeId) {
        if (id == null)
            throw new IllegalArgumentException("ConquistaId não pode ser nulo.");
        if (tutorId == null || tutorId.isBlank())
            throw new IllegalArgumentException("TutorId não pode ser vazio.");
        if (badgeId == null)
            throw new IllegalArgumentException("BadgeId não pode ser nulo.");
        this.id = id;
        this.tutorId = tutorId;
        this.badgeId = badgeId;
        this.conquistadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public ConquistaTutor(ConquistaId id, String tutorId, BadgeId badgeId,
                          LocalDateTime conquistadoEm) {
        this.id = id;
        this.tutorId = tutorId;
        this.badgeId = badgeId;
        this.conquistadoEm = conquistadoEm;
    }

    public ConquistaId getId()            { return id; }
    public String getTutorId()            { return tutorId; }
    public BadgeId getBadgeId()           { return badgeId; }
    public LocalDateTime getConquistadoEm() { return conquistadoEm; }
}
