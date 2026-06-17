package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PeriodoRenovacao;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficios_tutor")
public class BeneficioTutorJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String planoId;

    @Column(nullable = false)
    private String beneficioCatalogoId;

    @Column(nullable = false)
    private LocalDateTime dataLiberacao;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String periodoRenovacao;

    @Column(nullable = false)
    private int limiteUsosPorPeriodo;

    @Column(nullable = false)
    private int usosRestantesPeriodoAtual;

    @Column(nullable = false)
    private LocalDateTime inicioPeriodoAtual;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    protected BeneficioTutorJpa() {}

    public static BeneficioTutorJpa fromDomain(BeneficioTutor b) {
        BeneficioTutorJpa j = new BeneficioTutorJpa();
        j.id = b.getId().getValor();
        j.tutorId = b.getTutorId().getValor();
        j.planoId = b.getPlanoId().getValor();
        j.beneficioCatalogoId = b.getBeneficioCatalogoId().getValor();
        j.dataLiberacao = b.getDataLiberacao();
        j.status = b.getStatus().name();
        j.periodoRenovacao = b.getPeriodoRenovacao().name();
        j.limiteUsosPorPeriodo = b.getLimiteUsosPorPeriodo();
        j.usosRestantesPeriodoAtual = b.getUsosRestantesPeriodoAtual();
        j.inicioPeriodoAtual = b.getInicioPeriodoAtual();
        j.criadoEm = b.getCriadoEm();
        j.atualizadoEm = b.getAtualizadoEm();
        return j;
    }

    public BeneficioTutor toDomain() {
        return new BeneficioTutor(
                BeneficioTutorId.de(id),
                TutorId.de(tutorId),
                PlanoId.de(planoId),
                BeneficioCatalogoId.de(beneficioCatalogoId),
                dataLiberacao,
                StatusBeneficio.valueOf(status),
                PeriodoRenovacao.valueOf(periodoRenovacao),
                limiteUsosPorPeriodo,
                usosRestantesPeriodoAtual,
                inicioPeriodoAtual,
                criadoEm,
                atualizadoEm
        );
    }

    public String getId()                  { return id; }
    public String getTutorId()             { return tutorId; }
    public String getPlanoId()             { return planoId; }
    public String getBeneficioCatalogoId() { return beneficioCatalogoId; }
    public String getStatus()              { return status; }
}
