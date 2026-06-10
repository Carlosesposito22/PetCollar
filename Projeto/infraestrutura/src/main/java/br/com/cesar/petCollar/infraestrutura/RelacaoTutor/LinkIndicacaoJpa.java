package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CodigoIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "links_indicacao")
public class LinkIndicacaoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false, unique = true, length = 8)
    private String codigo;

    protected LinkIndicacaoJpa() {}

    public static LinkIndicacaoJpa fromDomain(LinkIndicacao link) {
        LinkIndicacaoJpa j = new LinkIndicacaoJpa();
        j.id = link.getId().getValor();
        j.tutorId = link.getTutorId().getValor();
        j.codigo = link.getCodigo().getValor();
        return j;
    }

    public LinkIndicacao toDomain() {
        return new LinkIndicacao(
            LinkIndicacaoId.de(id),
            TutorId.de(tutorId),
            CodigoIndicacao.de(codigo)
        );
    }

    public String getId()     { return id; }
    public String getTutorId(){ return tutorId; }
    public String getCodigo() { return codigo; }
}
