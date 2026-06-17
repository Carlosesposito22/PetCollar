package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CodigoIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "links_indicacao")
public class LinkIndicacaoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false, unique = true, length = 8)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    protected LinkIndicacaoJpa() {}

    public static LinkIndicacaoJpa fromDomain(LinkIndicacao link) {
        LinkIndicacaoJpa j = new LinkIndicacaoJpa();
        j.id = link.getId().getValor();
        j.tutorId = link.getTutorId().getValor();
        j.codigo = link.getCodigo().getValor();
        j.criadoEm = link.getCriadoEm();
        return j;
    }

    public LinkIndicacao toDomain() {
        return new LinkIndicacao(
            LinkIndicacaoId.de(id),
            TutorId.de(tutorId),
            CodigoIndicacao.de(codigo),
            criadoEm
        );
    }

    public String getId()             { return id; }
    public String getTutorId()        { return tutorId; }
    public String getCodigo()         { return codigo; }
    public LocalDateTime getCriadoEm(){ return criadoEm; }
}
