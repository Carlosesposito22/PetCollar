package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.RegistroClique;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.RegistroCliqueId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_clique_indicacao")
public class RegistroCliqueJpa {

    @Id
    private String id;

    @Column(nullable = false, length = 11)
    private String cpfIndicado;

    @Column(nullable = false)
    private String linkId;

    @Column(nullable = false)
    private String tutorIndicadorId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected RegistroCliqueJpa() {}

    public static RegistroCliqueJpa fromDomain(RegistroClique r) {
        RegistroCliqueJpa j = new RegistroCliqueJpa();
        j.id = r.getId().getValor();
        j.cpfIndicado = r.getCpfIndicado().getValor();
        j.linkId = r.getLinkId().getValor();
        j.tutorIndicadorId = r.getTutorIndicadorId().getValor();
        j.timestamp = r.getTimestamp();
        return j;
    }

    public RegistroClique toDomain() {
        return new RegistroClique(
            RegistroCliqueId.de(id),
            CPF.de(cpfIndicado),
            LinkIndicacaoId.de(linkId),
            TutorId.de(tutorIndicadorId),
            timestamp
        );
    }

    public String getId()              { return id; }
    public String getCpfIndicado()     { return cpfIndicado; }
    public String getTutorIndicadorId(){ return tutorIndicadorId; }
    public LocalDateTime getTimestamp(){ return timestamp; }
}
