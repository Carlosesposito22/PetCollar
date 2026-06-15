package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.Indicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.StatusIndicacao;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "indicacoes")
public class IndicacaoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorIndicadorId;

    @Column(nullable = false)
    private String linkId;

    @Column(nullable = false, length = 11)
    private String cpfIndicado;

    @Column(nullable = false)
    private LocalDateTime timestampClique;

    @Column(nullable = false)
    private String status;

    private String cobrancaIndicadorId;

    private LocalDateTime convertidaEm;

    private String motivoInvalidacao;

    protected IndicacaoJpa() {}

    public static IndicacaoJpa fromDomain(Indicacao ind) {
        IndicacaoJpa j = new IndicacaoJpa();
        j.id = ind.getId().getValor();
        j.tutorIndicadorId = ind.getTutorIndicadorId().getValor();
        j.linkId = ind.getLinkId().getValor();
        j.cpfIndicado = ind.getCpfIndicado().getValor();
        j.timestampClique = ind.getTimestampClique();
        j.status = ind.getStatus().name();
        j.cobrancaIndicadorId = ind.getCobrancaIndicadorId();
        j.convertidaEm = ind.getConvertidaEm();
        j.motivoInvalidacao = ind.getMotivoInvalidacao();
        return j;
    }

    public Indicacao toDomain() {
        return new Indicacao(
            IndicacaoId.de(id),
            TutorId.de(tutorIndicadorId),
            LinkIndicacaoId.de(linkId),
            CPF.de(cpfIndicado),
            timestampClique,
            StatusIndicacao.valueOf(status),
            cobrancaIndicadorId,
            convertidaEm,
            motivoInvalidacao
        );
    }

    public String getId()              { return id; }
    public String getTutorIndicadorId(){ return tutorIndicadorId; }
    public String getCpfIndicado()     { return cpfIndicado; }
    public String getStatus()          { return status; }
}
