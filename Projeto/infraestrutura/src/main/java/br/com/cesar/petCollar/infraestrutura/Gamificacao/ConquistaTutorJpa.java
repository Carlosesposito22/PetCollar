package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConquistaId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConquistaTutor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conquistas_tutor")
public class ConquistaTutorJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private LocalDateTime conquistadoEm;

    protected ConquistaTutorJpa() {}

    public static ConquistaTutorJpa fromDomain(ConquistaTutor c) {
        ConquistaTutorJpa j = new ConquistaTutorJpa();
        j.id = c.getId().getValor();
        j.tutorId = c.getTutorId();
        j.badgeId = c.getBadgeId().getValor();
        j.conquistadoEm = c.getConquistadoEm();
        return j;
    }

    public ConquistaTutor toDomain() {
        return new ConquistaTutor(
                ConquistaId.de(id),
                tutorId,
                BadgeId.de(badgeId),
                conquistadoEm
        );
    }

    public String getId()      { return id; }
    public String getTutorId() { return tutorId; }
    public String getBadgeId() { return badgeId; }
}
