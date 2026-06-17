package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ProgressoBadge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ProgressoBadgeId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "progressos_badge")
public class ProgressoBadgeJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private int valorAtual;

    @Column(nullable = false)
    private int metaTotal;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    protected ProgressoBadgeJpa() {}

    public static ProgressoBadgeJpa fromDomain(ProgressoBadge p) {
        ProgressoBadgeJpa j = new ProgressoBadgeJpa();
        j.id = p.getId().getValor();
        j.tutorId = p.getTutorId();
        j.badgeId = p.getBadgeId().getValor();
        j.valorAtual = p.getValorAtual();
        j.metaTotal = p.getMetaTotal();
        j.criadoEm = p.getCriadoEm();
        j.atualizadoEm = p.getAtualizadoEm();
        return j;
    }

    public ProgressoBadge toDomain() {
        return new ProgressoBadge(
                ProgressoBadgeId.de(id),
                tutorId,
                BadgeId.de(badgeId),
                valorAtual,
                metaTotal,
                criadoEm,
                atualizadoEm
        );
    }

    public String getId()      { return id; }
    public String getTutorId() { return tutorId; }
    public String getBadgeId() { return badgeId; }
}
